ALTER TABLE ARTICLES ADD COLUMN (
  PUBLISHED  BOOLEAN NOT NULL DEFAULT FALSE,
  HAS_DRAFT  BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE USERS DROP COLUMN IMG_URL; 
CREATE INDEX IDX_USERS2 ON USERS (LOGIN_ID);
