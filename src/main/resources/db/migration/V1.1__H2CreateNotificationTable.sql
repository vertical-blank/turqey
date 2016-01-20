CREATE TABLE STOCK_NOTIFICATIONS (
  ID          bigint auto_increment primary key,
  ARTICLE_ID  bigint NOT NULL,
  USER_ID     bigint NOT NULL
);

CREATE TABLE COMMENT_NOTIFICATIONS (
  ID          bigint auto_increment primary key,
  COMMENT_ID  bigint NOT NULL
);

CREATE TABLE ARTICLE_NOTIFICATIONS (
  ID          bigint auto_increment primary key,
  ARTICLE_ID  bigint NOT NULL
);

CREATE TABLE USER_FOLLOWINGS (
  ID          bigint auto_increment primary key,
  USER_ID     bigint NOT NULL,
  FOLLOWED_ID bigint NOT NULL
);

CREATE TABLE TAG_FOLLOWINGS (
  ID          bigint auto_increment primary key,
  USER_ID     bigint NOT NULL,
  FOLLOWED_ID bigint NOT NULL
);

CREATE TABLE FOLLOW_NOTIFICATIONS (
  ID          bigint auto_increment primary key,
  USER_ID     bigint NOT NULL
);

