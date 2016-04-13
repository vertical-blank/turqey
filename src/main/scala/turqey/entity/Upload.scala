package turqey.entity

import scalikejdbc._

case class Upload(
  id: Long,
  name: String,
  mime: String,
  isImage: Boolean,
  size: Long,
  ownerId: Long) {

  def save()(implicit session: DBSession = Upload.autoSession): Upload = Upload.save(this)(session)

  def destroy()(implicit session: DBSession = Upload.autoSession): Unit = Upload.destroy(this)(session)

}


object Upload extends SQLSyntaxSupport[Upload] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "UPLOADS"

  override val columns = Seq("ID", "NAME", "MIME", "IS_IMAGE", "SIZE", "OWNER_ID")

  def apply(u: SyntaxProvider[Upload])(rs: WrappedResultSet): Upload = apply(u.resultName)(rs)
  def apply(u: ResultName[Upload])(rs: WrappedResultSet): Upload = new Upload(
    id = rs.get(u.id),
    name = rs.get(u.name),
    mime = rs.get(u.mime),
    isImage = rs.get(u.isImage),
    size = rs.get(u.size),
    ownerId = rs.get(u.ownerId)
  )

  val u = Upload.syntax("u")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Upload] = {
    withSQL {
      select.from(Upload as u).where.eq(u.id, id)
    }.map(Upload(u.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Upload] = {
    withSQL(select.from(Upload as u)).map(Upload(u.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Upload as u)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Upload] = {
    withSQL {
      select.from(Upload as u).where.append(where)
    }.map(Upload(u.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Upload] = {
    withSQL {
      select.from(Upload as u).where.append(where)
    }.map(Upload(u.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Upload as u).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    name: String,
    mime: String,
    isImage: Boolean,
    size: Long,
    ownerId: Long)(implicit session: DBSession = autoSession): Upload = {
    val generatedKey = withSQL {
      insert.into(Upload).columns(
        column.name,
        column.mime,
        column.isImage,
        column.size,
        column.ownerId
      ).values(
        name,
        mime,
        isImage,
        size,
        ownerId
      )
    }.updateAndReturnGeneratedKey.apply()

    Upload(
      id = generatedKey,
      name = name,
      mime = mime,
      isImage = isImage,
      size = size,
      ownerId = ownerId)
  }

  def batchInsert(entities: Seq[Upload])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'name -> entity.name,
        'mime -> entity.mime,
        'isImage -> entity.isImage,
        'size -> entity.size,
        'ownerId -> entity.ownerId))
        SQL("""insert into UPLOADS(
        NAME,
        MIME,
        IS_IMAGE,
        SIZE,
        OWNER_ID
      ) values (
        {name},
        {mime},
        {isImage},
        {size},
        {ownerId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: Upload)(implicit session: DBSession = autoSession): Upload = {
    withSQL {
      update(Upload).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.mime -> entity.mime,
        column.isImage -> entity.isImage,
        column.size -> entity.size,
        column.ownerId -> entity.ownerId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Upload)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Upload).where.eq(column.id, entity.id) }.update.apply()
  }

}
