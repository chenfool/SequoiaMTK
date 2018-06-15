package com.sequoiadb;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CheckDBTest {
    public final static String comm = "CREATE TABLE `t1` (\n" +
            "  `name` varchar(100) DEFAULT NULL,\n" +
            "  `id` int(11) NOT NULL DEFAULT '0',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=gbk";
    
    
    @Test
    public void testGetCharset () {
        String _comm = comm.toLowerCase();
        assertEquals("gbk", CheckDB.getCharset (_comm));
    }
    
    @Test
    public void testChangeCharsetToUtf8 () {
        String comm2 = "CREATE TABLE `t1` (\n" +
                "  `name` varchar(100) DEFAULT NULL,\n" +
                "  `id` int(11) NOT NULL DEFAULT '0',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
        comm2 = comm2.toLowerCase();
        String _comm = comm.toLowerCase();
        assertEquals(comm2, CheckDB.changeCharsetToUtf8 (_comm));
    }
    
    @Test
    public void testChangeEngine () {
        String comm2 = "CREATE TABLE `t1` (\n" +
            "  `name` varchar(100) DEFAULT NULL,\n" +
            "  `id` int(11) NOT NULL DEFAULT '0',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=sequoiadb DEFAULT CHARSET=gbk";
        comm2 = comm2.toLowerCase();
        String _comm = comm.toLowerCase();
        assertEquals(comm2, CheckDB.changeEngine (_comm));
    }
    
    @Test
    public void testAddComment () {
        
        String shardingKey = "testCol";
        String comm2 = "CREATE TABLE `t1` (\n" +
                "  `name` varchar(100) DEFAULT NULL,\n" +
                "  `id` int(11) NOT NULL DEFAULT '0',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=gbk" +
                " comment=\"{table_options:{Compressed:true, CompressionType:'lzw', ShardingKey:{'" + shardingKey +"':1}, ShardingType:'hash'}}\" ;";
        
        assertEquals (comm2, CheckDB.addComment(comm, shardingKey));
    }
    
    @Test
    public void testGetFirstColumn () {
        String _comm = comm.toLowerCase();
        assertEquals ("name", CheckDB.getFirstColumn(_comm));
    }

    @Test
    public void testGetPrimaryKey () {
        String _comm = comm.toLowerCase();
        assertEquals ("id", CheckDB.getPrimaryKey(_comm));
    }


    @Test
    public void testCheckHavePrimaryKey () {
        String _comm = comm.toLowerCase();
        assertEquals(true, CheckDB.checkHavePrimaryKey (_comm));
    }
    
    @Test
    public void testCheckColType () {
        assertEquals (true, CheckDB.checkColType("tinyint"));
        assertEquals (true, CheckDB.checkColType("smallint"));
        assertEquals (true, CheckDB.checkColType("mediumint"));
        assertEquals (true, CheckDB.checkColType("int"));
        assertEquals (true, CheckDB.checkColType("bigint"));
        assertEquals (true, CheckDB.checkColType("float"));
        assertEquals (true, CheckDB.checkColType("double"));
        assertEquals (true, CheckDB.checkColType("decimal"));
        assertEquals (true, CheckDB.checkColType("date"));
        assertEquals (true, CheckDB.checkColType("datetime"));
        assertEquals (true, CheckDB.checkColType("timestamp"));
        assertEquals (true, CheckDB.checkColType("char"));
        assertEquals (true, CheckDB.checkColType("varchar"));
        assertEquals (true, CheckDB.checkColType("text"));
        assertEquals (true, CheckDB.checkColType("binary"));
        assertEquals (true, CheckDB.checkColType("blob"));
        assertEquals (false, CheckDB.checkColType(""));
        assertEquals (false, CheckDB.checkColType("longtext"));
    }
    
    
    
}
