CREATE TABLE FILE_ACCESS_LOG  (
   ACCESS_TOKEN       VARCHAR(36)                    NOT NULL,
   FILE_ID              VARCHAR(36),
   AUTH_TIME          DATETIME                            NOT NULL,
   ACCESS_USERCODE    VARCHAR(8),
   ACCESS_USENAME     VARCHAR(50),
   ACCESS_RIGHT       VARCHAR(2)                     NOT NULL COMMENT 
'A�� ����Ȩ��  S: ����Դ�ļ�  T �����ظ����ļ� ' ,
   TOKEN_EXPIRE_TIME  DATETIME                            NOT NULL,
   ACCESS_TIMES       DECIMAL(6)                      DEFAULT 0 NOT NULL,
   LAST_ACCESS_TIME   DATETIME,
   LAST_ACCESS_HOST   VARCHAR(100),
    PRIMARY KEY (ACCESS_TOKEN)
);


CREATE TABLE FILE_STORE_INFO  (
   FILE_ID              VARCHAR(36)                    NOT NULL,
   FILE_MD5             VARCHAR(36) COMMENT '�ļ�MD5����' ,
   FILE_NAME          VARCHAR(200) COMMENT 'ԭʼ�ļ�����',
   FILE_SHOW_PATH     VARCHAR(1000),
   FILE_STORE_PATH    VARCHAR(200),
   FILE_TYPE          VARCHAR(8) COMMENT '�ļ���׺��',
   FILE_DESC          VARCHAR(200),
   FILE_STATE         CHAR COMMENT 'C : �����ϴ�  N : ���� Z:���ļ� F:�ļ��ϴ�ʧ��',
   FILE_SIZE          DECIMAL(20),
   DOWNLOAD_TIMES     DECIMAL(6),
   OS_ID                VARCHAR(20),
   OPT_ID             VARCHAR(64)                    NOT NULL COMMENT 'ģ�飬���߱�',
   OPT_METHOD         VARCHAR(64)  COMMENT '�����������ֶ�',
   OPT_TAG            VARCHAR(200) COMMENT 'һ�����ڹ�����ҵ������',
   CREATED            VARCHAR(8),
   CREATE_TIME        DATETIME,
   INDEX_STATE        CHAR COMMENT 'N ������Ҫ���� S���ȴ����� I�������� F:����ʧ��',
   ENCRYPT_TYPE       CHAR COMMENT 'N : û�м���   Z��ZIPFILE    D:DES����',
   FILE_OWNER         VARCHAR(32),
   FILE_UNIT          VARCHAR(32),
   ATTACHED_STORE_PATH VARCHAR(200),
   ATTACHED_TYPE      VARCHAR(1) COMMENT '�����ļ����N :   û��  T������ͼ  P�� PDFֻ���ļ�',
    PRIMARY KEY (FILE_ID)
);


CREATE INDEX INDEX_FILE_MD5 ON FILE_STORE_INFO (
   FILE_MD5 ASC
);

