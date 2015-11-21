CREATE TABLE PROJECTS (
  ID     bigint auto_increment primary key,
  NAME   varchar(255) NOT NULL,
  OWNER  bigint NOT NULL
);

CREATE TABLE ARTICLES (
  ID          bigint auto_increment primary key,
  PROJECT_ID  bigint,
  TITLE       varchar(255) NOT NULL,
  CONTENT     CLOB NOT NULL,
  OWNER       bigint NOT NULL,
  CREATED     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IDX_ARTICLES1 ON ARTICLES (PROJECT_ID);
CREATE INDEX IDX_ARTICLES2 ON ARTICLES (TITLE);

CREATE TABLE ARTICLE_HISTORIES (
  ID          bigint auto_increment primary key,
  ARTICLE_ID  bigint NOT NULL,
  DIFF        CLOB NOT NULL,
  USER_ID     bigint,
  CREATED     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IDX_ARTICLE_HISTORIES1 ON ARTICLE_HISTORIES (ARTICLE_ID);

CREATE TABLE ARTICLE_COMMENTS (
  ID          bigint auto_increment primary key,
  ARTICLE_ID  bigint NOT NULL,
  USER_ID     bigint NOT NULL,
  CONTENT     CLOB NOT NULL,
  CREATED     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IDX_ARTICLE_COMMENTS1 ON ARTICLE_COMMENTS (ARTICLE_ID);
CREATE INDEX IDX_ARTICLE_COMMENTS2 ON ARTICLE_COMMENTS (USER_ID);

CREATE TABLE ARTICLE_SHARINGS (
  ID         bigint auto_increment primary key,
  PARENT_ID  bigint NOT NULL,
  USER_ID    bigint,
  GROUP_ID   bigint
);
CREATE INDEX IDX_ARTICLE_SHARINGS1 ON ARTICLE_SHARINGS (PARENT_ID);
CREATE INDEX IDX_ARTICLE_SHARINGS2 ON ARTICLE_SHARINGS (USER_ID);
CREATE INDEX IDX_ARTICLE_SHARINGS3 ON ARTICLE_SHARINGS (GROUP_ID);

CREATE TABLE GROUPS (
  ID         bigint auto_increment primary key,
  NAME       varchar(255) NOT NULL,
  OWNER      bigint NOT NULL
);

CREATE TABLE GROUP_MEMBERS (
  ID              bigint auto_increment primary key,
  GROUP_ID        bigint NOT NULL,
  MEMBER_USER_ID  bigint, 
  MEMBER_GROUP_ID bigint
);
CREATE INDEX IDX_GROUP_MEMBERS1 ON GROUP_MEMBERS (GROUP_ID);
CREATE INDEX IDX_GROUP_MEMBERS2 ON GROUP_MEMBERS (MEMBER_USER_ID);
CREATE INDEX IDX_GROUP_MEMBERS3 ON GROUP_MEMBERS (MEMBER_GROUP_ID);

CREATE TABLE USERS (
  ID          bigint auto_increment primary key,
  EMAIL       varchar(255) NOT NULL UNIQUE,
  NAME        varchar(255) NOT NULL,
  IMG_URL     varchar(255) NOT NULL,
  PASSWORD    varchar(255),
  TYPE        INTEGER,
  LAST_LOGIN  TIMESTAMP,
  CREATED     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TAGS (
  ID          bigint auto_increment primary key,
  NAME        varchar(255) NOT NULL UNIQUE
);

CREATE TABLE ARTICLE_TAGGINGS (
  ID          bigint auto_increment primary key,
  ARTICLE_ID  bigint NOT NULL,
  TAG_ID      bigint NOT NULL,
  CREATED     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IDX_ARTICLE_TAGGINGS1 ON ARTICLE_TAGGINGS (ARTICLE_ID);
CREATE INDEX IDX_ARTICLE_TAGGINGS2 ON ARTICLE_TAGGINGS (TAG_ID);

CREATE TABLE ARTICLE_STOCKS (
  ID          bigint auto_increment primary key,
  ARTICLE_ID  bigint NOT NULL,
  USER_ID     bigint NOT NULL
);
CREATE INDEX IDX_ARTICLE_STOCKS1 ON ARTICLE_STOCKS (ARTICLE_ID);
CREATE INDEX IDX_ARTICLE_STOCKS2 ON ARTICLE_STOCKS (USER_ID);

