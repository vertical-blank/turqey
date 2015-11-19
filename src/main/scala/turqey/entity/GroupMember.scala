package turqey.entity

import scalikejdbc._

case class GroupMember(
  id: Long,
  groupId: Long,
  memberUserId: Option[Long] = None,
  memberGroupId: Option[Long] = None) {

  def save()(implicit session: DBSession = GroupMember.autoSession): GroupMember = GroupMember.save(this)(session)

  def destroy()(implicit session: DBSession = GroupMember.autoSession): Unit = GroupMember.destroy(this)(session)

}


object GroupMember extends SQLSyntaxSupport[GroupMember] {

  override val tableName = "GROUP_MEMBERS"

  override val columns = Seq("ID", "GROUP_ID", "MEMBER_USER_ID", "MEMBER_GROUP_ID")

  def apply(gm: SyntaxProvider[GroupMember])(rs: WrappedResultSet): GroupMember = apply(gm.resultName)(rs)
  def apply(gm: ResultName[GroupMember])(rs: WrappedResultSet): GroupMember = new GroupMember(
    id = rs.get(gm.id),
    groupId = rs.get(gm.groupId),
    memberUserId = rs.get(gm.memberUserId),
    memberGroupId = rs.get(gm.memberGroupId)
  )

  val gm = GroupMember.syntax("gm")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[GroupMember] = {
    withSQL {
      select.from(GroupMember as gm).where.eq(gm.id, id)
    }.map(GroupMember(gm.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[GroupMember] = {
    withSQL(select.from(GroupMember as gm)).map(GroupMember(gm.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(GroupMember as gm)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[GroupMember] = {
    withSQL {
      select.from(GroupMember as gm).where.append(where)
    }.map(GroupMember(gm.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[GroupMember] = {
    withSQL {
      select.from(GroupMember as gm).where.append(where)
    }.map(GroupMember(gm.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(GroupMember as gm).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    groupId: Long,
    memberUserId: Option[Long] = None,
    memberGroupId: Option[Long] = None)(implicit session: DBSession = autoSession): GroupMember = {
    val generatedKey = withSQL {
      insert.into(GroupMember).columns(
        column.groupId,
        column.memberUserId,
        column.memberGroupId
      ).values(
        groupId,
        memberUserId,
        memberGroupId
      )
    }.updateAndReturnGeneratedKey.apply()

    GroupMember(
      id = generatedKey,
      groupId = groupId,
      memberUserId = memberUserId,
      memberGroupId = memberGroupId)
  }

  def batchInsert(entities: Seq[GroupMember])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'groupId -> entity.groupId,
        'memberUserId -> entity.memberUserId,
        'memberGroupId -> entity.memberGroupId))
        SQL("""insert into GROUP_MEMBERS(
        GROUP_ID,
        MEMBER_USER_ID,
        MEMBER_GROUP_ID
      ) values (
        {groupId},
        {memberUserId},
        {memberGroupId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: GroupMember)(implicit session: DBSession = autoSession): GroupMember = {
    withSQL {
      update(GroupMember).set(
        column.id -> entity.id,
        column.groupId -> entity.groupId,
        column.memberUserId -> entity.memberUserId,
        column.memberGroupId -> entity.memberGroupId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: GroupMember)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(GroupMember).where.eq(column.id, entity.id) }.update.apply()
  }

}
