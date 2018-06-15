# SequoiaMTK
SequoiaDB Migration Tookits

# mexport
mysql 的数据迁移工具，使用shell和java程序从mysql中将database的数据和建表语句导出到本地文件，方便用户实现mysql数据库的数据迁移。

## mexport 使用方法
```
-i              mysql ip, default "localhost"
-p              mysql port, default 3306
-u              mysql user
-w              mysql user's password
-d              mysql dbname
-o              [true/false] overwrite export path, default true
--trans         [true/false] gbk trans to utf8,default true
-h, --help print help info
```

## mexport 运行原理
mexport 在检查环境问题后，将调用java 程序从mysql 中读取指定database所有表的信息，并且将信息写入到metatable.out 文件中。\<br>
mexport 将根据metatable.out文件从mysql中导出数据，并且根据SequoiaSQL的语法特点生成create_table.sql(建表语句)和sdbImport.sh(SequoiaDB导入命令)

## mexport 运行要求
程序只能够运行在linux环境下。\<br>
环境需要预先安装JDK 1.7+。\<br>
由于目前版本mexport从mysql中导出数据是使用mysql提供的outfile功能，所以mysql服务必须要预先设置secure_file_priv参数，否则将会数据导出失败。

## mexport 注意
mexport在检查mysql 的表结构时，如果发现数据表中字段类型不支持导出，则在后续的处理中将会忽略该数据表。
目前mexport支持的数据类型包括
* tinyint
* smallint
* mediumint
* int
* bigint
* float
* double
* decimal
* date
* datetime
* timestamp
* char
* varchar
* text
另外，如果数据表的字符集非utf8和gbk同样也无法导出数据，并且当字符集为gbk时，mexport将自动将导出文件的字符集转换为utf8。
