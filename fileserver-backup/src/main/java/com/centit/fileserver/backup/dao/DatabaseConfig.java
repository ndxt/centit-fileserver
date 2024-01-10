package com.centit.fileserver.backup.dao;

import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.database.metadata.JdbcMetadata;
import com.centit.support.database.metadata.SimpleTableInfo;
import com.centit.support.database.utils.DBType;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.QueryLogUtils;
import com.centit.support.security.SecurityOptUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseConfig {

    protected static Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    public static String fileRootPath;
    public static Properties loadProperties() {
        Properties prop = new Properties();
        try(InputStream resource = DatabaseConfig
            .class.getResourceAsStream("system.properties")){
            //new ClassPathResource("system.properties").getInputStream();
            if(resource==null) {
                try(InputStream resource2 = ClassLoader.getSystemResourceAsStream("system.properties")){
                    if(resource2 != null) {
                        prop.load(resource2);
                    }
                }
            }else {
                prop.load(resource);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileRootPath = prop.getProperty("file.root.path");
        if(!fileRootPath.endsWith("/") && !fileRootPath.endsWith("\\")){
            fileRootPath = fileRootPath + File.separatorChar;
        }
        return prop;
    }

    public static DataSource createDataSource() {
        Properties env = loadProperties();
        HikariDataSource ds = new HikariDataSource();
        //失败时是否进行重试连接    true:不进行重试   false：进行重试    设置为false时达蒙数据库会出现问题（会导致达蒙连接撑爆挂掉）
        ds.setDriverClassName(env.getProperty("jdbc.driver")); //getDbDriver(dbType)
        ds.setUsername(SecurityOptUtils.decodeSecurityString(env.getProperty("jdbc.user")));
        ds.setPassword(SecurityOptUtils.decodeSecurityString(env.getProperty("jdbc.password")));
        ds.setJdbcUrl(env.getProperty("jdbc.url"));
        ds.setMaxLifetime(NumberBaseOpt.castObjectToInteger(env.getProperty("jdbc.maxLifeTime"), 18000));
        ds.setMaximumPoolSize(NumberBaseOpt.castObjectToInteger(env.getProperty("jdbc.maxActive"),100));
        ds.setConnectionTimeout(NumberBaseOpt.castObjectToInteger(env.getProperty("jdbc.maxWait"), 5000));
        ds.setMinimumIdle(NumberBaseOpt.castObjectToInteger(env.getProperty("jdbc.minIdle"), 5));
        ds.setValidationTimeout(NumberBaseOpt.castObjectToInteger(env.getProperty("jdbc.validationTimeout"), 60000));

        ds.setAutoCommit(true);

        DBType dbType = DBType.mapDBType(env.getProperty("jdbc.url"));
        String validationQuery = env.getProperty("jdbc.validationQuery");

        boolean testWhileIdle = BooleanBaseOpt.castObjectToBoolean(
            env.getProperty("jdbc.testWhileIdle"),true);
        if(StringUtils.isBlank(validationQuery)){
            validationQuery = DBType.getDBValidationQuery(dbType);
        }

        if (testWhileIdle && StringUtils.isNotBlank(validationQuery)){
            ds.setConnectionTestQuery(validationQuery);
        }
        if (StringRegularOpt.isTrue(env.getProperty("jdbc.show.sql"))) {
            QueryLogUtils.setJdbcShowSql(true);
        }
        return ds;
    }

    public static Integer checkBackupTables(Connection dbc) throws SQLException {
        JdbcMetadata metadata = new JdbcMetadata();
        metadata.setDBConfig(dbc);
        SimpleTableInfo tab = metadata.getTableMetadata("FILE_BACKUP_INFO");
        if(tab==null){
            List<String> createTableSqls = new ArrayList<>(16);
            DBType dbType = DBType.mapDBType(dbc);
            switch (dbType){
                case DM:
                case Oracle:
                case KingBase:
                case GBase:
                case Oscar:
                    createTableSqls.add("create table FILE_BACKUP_INFO (\n" +
                        "  BACKUP_ID varchar2(32) not null,\n" +
                        "  OS_ID varchar2(32),\n" +
                        "  DEST_PATH varchar2(300),\n" +
                        "  BEGIN_TIME Date,\n" +
                        "  END_TIME Date,\n" +
                        "  CREATE_TIME Date,\n" +
                        "  COMPLETE_TIME Date,\n" +
                       // "  STATUS varchar2(1),\n" +
                        "  FILE_COUNT number(12),\n" +
                        "  SUCCESS_COUNT number(12),\n" +
                        "  ERROR_COUNT number(12)," +
                        "  constraint PK_FILE_BACKUP_INFO primary key (BACKUP_ID))");
                    createTableSqls.add("create table FILE_BACKUP_LIST (\n" +
                        "  BACKUP_ID varchar2(32) not null,\n" +
                        "  FILE_ID varchar2(32) not null,\n" +
                        "  BACKUP_STATUS varchar2(1),\n" +
                        "  constraint PK_FILE_BACKUP_LIST primary key (BACKUP_ID, FILE_ID))");
                    break;
                case DB2:
                case PostgreSql:
                    createTableSqls.add("create table FILE_BACKUP_INFO (\n" +
                        "  BACKUP_ID varchar(32) not null,\n" +
                        "  OS_ID varchar(32),\n" +
                        "  DEST_PATH varchar(300),\n" +
                        "  BEGIN_TIME Date,\n" +
                        "  END_TIME Date,\n" +
                        "  CREATE_TIME Date,\n" +
                        "  COMPLETE_TIME Date,\n" +
                       // "  STATUS varchar(1),\n" +
                        "  FILE_COUNT integer,\n" +
                        "  SUCCESS_COUNT INTEGER,\n" +
                        "  ERROR_COUNT INTEGER," +
                        "  constraint PK_FILE_BACKUP_INFO primary key (BACKUP_ID))");
                    createTableSqls.add("create table FILE_BACKUP_LIST (\n" +
                        "  BACKUP_ID varchar(32) not null,\n" +
                        "  FILE_ID varchar(32) not null,\n" +
                        "  BACKUP_STATUS varchar(1),\n" +
                        "  constraint PK_FILE_BACKUP_LIST primary key (BACKUP_ID, FILE_ID))");
                        break;
                case MySql:
                default:
                    createTableSqls.add("create table FILE_BACKUP_INFO (\n" +
                        "  BACKUP_ID varchar(32) not null comment '备份ID',\n" +
                        "  OS_ID varchar(32) comment '应用系统',\n" +
                        "  DEST_PATH varchar(300) comment '目标地址',\n" +
                        "  BEGIN_TIME Date comment '数据开始时间',\n" +
                        "  END_TIME Date comment '数据结束时间',\n" +
                        "  CREATE_TIME Date comment '任务创建时间',\n" +
                        "  COMPLETE_TIME Date comment '任务完成时间',\n" +
                       // "  STATUS varchar(1) comment '任务状态',\n" +
                        "  FILE_COUNT INT comment '备份文件数',\n" +
                        "  SUCCESS_COUNT INT comment '成功备份数量',\n" +
                        "  ERROR_COUNT INT comment '备份失败数量'," +
                        "  primary key (BACKUP_ID))");
                    createTableSqls.add("create table FILE_BACKUP_LIST (\n" +
                        "  BACKUP_ID varchar(32) not null comment '备份ID',\n" +
                        "  FILE_ID varchar(32) not null comment '文件ID',\n" +
                        "  BACKUP_STATUS varchar(1) comment '状态',\n" +
                        "  primary key (BACKUP_ID, FILE_ID))");
                    break;
            }
            for(String sql : createTableSqls){
                DatabaseAccess.doExecuteSql(dbc, sql);
            }
            return 1;
        }
        return 0;
    }
}
