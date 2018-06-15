package com.sequoiadb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CheckDB {
	
	public final static String METADATAFILE = "./metatable.out";
	public final static String CREATESQLFILE = "./create_table.sql";
	public final static Logger logger = LoggerFactory.getLogger (CheckDB.class);

	public static void main(String[] args) {

		Statement stmt = null;
		Connection conn = null;
		FileWriter fw_meta = null;
		FileWriter fw_create = null;

		try {
//			Class.forName("com.mysql.jdbc.Driver");
			
			if (args.length != 5) {
				logger.error ("Please import right ip & port & user & password & database");
				System.exit(1);
			}

			String ip = args[0];
			String port = args[1];
			String user = args[2];
			String password = args[3];
		    String database = args[4];
			
			String url = "jdbc:mysql://"+ ip + ":" + port + "/" + database ;


			/*
			url = "jdbc:mysql://192.168.137.131:3306/test";
			user = "test1";
			password = "abc";
			database = "test";
			*/
			conn = DriverManager.getConnection(url, user, password);
			stmt = conn.createStatement();
			logger.info ("connect to mysql, database = " + database);
			showTables(conn, stmt, database, fw_meta, fw_create);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fw_meta != null) {
					fw_meta.close();
				}
				if (fw_create != null) {
					fw_create.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void showTables(Connection conn, Statement stmt, String database, FileWriter fw_meta, FileWriter fw_create) throws ClassNotFoundException, SQLException, IOException {
		ResultSet rs = null;
        ResultSet rs2 = null;
		String col_type = "";
		String col_name = "";
		fw_meta = new FileWriter(METADATAFILE);
		
		fw_create = new FileWriter(CREATESQLFILE);

		BufferedWriter w_meta = new BufferedWriter(fw_meta);
		BufferedWriter w_create = new BufferedWriter(fw_create);

        boolean checkResult = false;
		try {
			rs = stmt.executeQuery("show tables");
			List<String> tabName_list = new ArrayList<String>();
			while (rs.next()) {
				tabName_list.add(rs.getString("Tables_in_" + database));
			}
			logger.debug ("The following tables is available under the database : " + tabName_list.toString());
			rs.close();
			DatabaseMetaData metaData = conn.getMetaData();
			for (int i = 0; i < tabName_list.size(); i++) {
                String tabName = tabName_list.get(i).toLowerCase();
                String charset = "";
                rs2 = stmt.executeQuery("show create table " + tabName);
                rs2.next();
                String comm = rs2.getString (2).toLowerCase();
				rs2.close();
				charset = getCharset (comm);
				comm = changeCharsetToUtf8 (comm);
                comm = changeEngine (comm);
				boolean havePrimaryKey = checkHavePrimaryKey (comm);
				String shardingKey = "";
				if (havePrimaryKey) {
					// sharding key = primary key
					shardingKey = getPrimaryKey (comm);
				} else {
					// sharding key = first column
					shardingKey = getFirstColumn (comm);
				}
                comm = addComment (comm, shardingKey);
                
                logger.debug ("" + database + "." + 
						      tabName + "'s charset = " + charset + " ");
                logger.debug ("" + database + "." + 
				              tabName + "'s create table sql = " + comm);
                
				ResultSet resultSet = metaData.getColumns(null, null, tabName, null);
                
                
				ArrayList<String> col_list = new ArrayList<String>();
				while (resultSet.next()) {
					col_name = resultSet.getString("COLUMN_NAME");
					col_type = resultSet.getString("TYPE_NAME").toLowerCase();
					col_list.add(col_name + ":" + col_type);
                    checkResult = checkColType(col_type);
                    if (!checkResult) break;
				}
                if (checkResult && (charset.equalsIgnoreCase("utf8") || charset.equalsIgnoreCase("gbk"))) {
					if (havePrimaryKey)
						logger.info ("check " + database + "." + tabName + "\tpass");
					else {
						String _print = "";
						if (charset.equalsIgnoreCase("gbk")) {
							_print = _print + "charset is gbk, ";
						}
						_print = _print + "but it don't have primary key";
						logger.warn("check " + database + "." + tabName + "\tpass, " + _print);
					}


					w_create.write(comm + "\n\n\n");

					w_meta.write (tabName + "|"
                            + col_list.toString().toLowerCase().replaceAll(" ", "") + "|"
                            + charset + "|" 
                            + "true");
                }
                else {
					String _t = "check " + database + "." + tabName + "\tfail";
					if (!checkResult) {
						logger.error (_t + ", " + database + "." + tabName + " have unsupported column type, column name = " 
								+ col_name + ", column type = " + col_type);
					}
//					else if (!charset.equalsIgnoreCase("utf8")) {
//						logger.error (_t + ", " + database + "." + tabName + "'s charset is " + charset + ", only support utf8 charset");
//					}
					else {
						logger.error (_t + ", " + "check " + database + "." + tabName + ", unknow error");
					}

					w_meta.write (tabName + "|"
                            + col_list.toString().toLowerCase().replaceAll(" ", "") + "|"
                            + charset + "|" 
                            + "false");
                }
				w_meta.write ("\n");
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (w_meta != null) {
				w_meta.close();
			}
			if (w_create != null) {
				w_create.close();
			}
			
			if (rs != null) {
				rs.close();
			}
		}
	}
	
	public static String getCharset (String comm) {
        int indexNum_begin = comm.indexOf( "charset=", 0);
        indexNum_begin = indexNum_begin + 8;
        int indexNum_end = comm.indexOf( " ", indexNum_begin);
        if (indexNum_end == -1) {
			indexNum_end = comm.indexOf ("\n", indexNum_begin);
			if (indexNum_end == -1) {
				indexNum_end = comm.length();
			}
		}
        String charset = comm.substring(indexNum_begin, indexNum_end);
        return charset;
    }
    
    public static String changeCharsetToUtf8 (String comm) {
		int indexNum_begin = comm.indexOf ("charset", 0);
		indexNum_begin = indexNum_begin + 8;
		int indexNum_end = comm.indexOf(" ", indexNum_begin);
		if (indexNum_end == -1) {
			indexNum_end = comm.indexOf("\n", indexNum_begin);
			if (indexNum_end == -1) {
				indexNum_end = comm.length();
			}
		}

		String charset = comm.substring (indexNum_begin, indexNum_end);
		String _comm = comm.replace ("charset=" + charset, "charset=utf8");
		return _comm;
	}
    
    public static String changeEngine (String comm) {
		int indexNum_begin = comm.indexOf("engine", 0);
		indexNum_begin = indexNum_begin + 7;

		int indexNum_end = comm.indexOf(" ", indexNum_begin);
		if (indexNum_end == -1) {
			indexNum_end = comm.indexOf ("\n", indexNum_begin);
			if (indexNum_end == -1) {
				indexNum_end = comm.length();
			}
		}
		String engine = comm.substring (indexNum_begin, indexNum_end);
		String _comm = comm.replace ("engine=" + engine, "engine=sequoiadb");
		return _comm;
	}
	
	public static String addComment (String comm, String shardingKey) {
		return comm + " comment=\"{table_options:{Compressed:true, CompressionType:'lzw', ShardingKey:{'" + shardingKey +"':1}, ShardingType:'hash'}}\" ;";
	}
	
	public static String getFirstColumn (String comm) {
		int num_begin = -1;
		int num_end = -1;
		String firstCol = "";
		num_begin = comm.indexOf ("(", 0);
		num_begin = comm.indexOf ("`", num_begin);
		num_end = comm.indexOf ("`", num_begin + 1);
		firstCol = comm.substring (num_begin + 1, num_end);
		return firstCol;
	}
	
	public static String getPrimaryKey (String comm) {
		int num_begin = comm.indexOf ("primary key", 0);
		int num_end = -1;
		String primaryKey = "";

		if (num_begin == -1)
			return "";
		else {
			num_begin = comm.indexOf ("`", num_begin);
			num_end = comm.indexOf ("`", num_begin + 1);
			primaryKey = comm.substring(num_begin + 1, num_end);
		}
		return primaryKey;
	}
	
	public static boolean checkHavePrimaryKey (String comm) {
		int num = comm.indexOf ("primary key", 0);
		
		if (num == -1) 
			return false;
		else
			return true;
	}
	
	public static boolean checkColType (String col_type) {
        if (col_type.equalsIgnoreCase("tinyint")) {
            return true;
        } else if (col_type.equalsIgnoreCase("smallint")) {
            return true;
        } else if (col_type.equalsIgnoreCase("mediumint")) {
            return true;
        } else if (col_type.equalsIgnoreCase("int")) {
            return true;
        } else if (col_type.equalsIgnoreCase("bigint")) {
            return true;
        } else if (col_type.equalsIgnoreCase("float")) {
            return true;
        } else if (col_type.equalsIgnoreCase("double")) {
            return true;
        } else if (col_type.equalsIgnoreCase("decimal")) {
            return true;
        } else if (col_type.equalsIgnoreCase("date")) {
            return true;
        } else if (col_type.equalsIgnoreCase("datetime")) {
            return true;
        } else if (col_type.equalsIgnoreCase("timestamp")) {
            return true;
        } else if (col_type.equalsIgnoreCase("char")) {
            return true;
        } else if (col_type.equalsIgnoreCase("varchar")) {
            return true;
        } else if (col_type.equalsIgnoreCase("text")) {
            return true;
        } 
        else if (col_type.equalsIgnoreCase("binary")) {
            return true;
        } else if (col_type.equalsIgnoreCase("blob")) {
        	return true;
		}
		return false;
	}
}
