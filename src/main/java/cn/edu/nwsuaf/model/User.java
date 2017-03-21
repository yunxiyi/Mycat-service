package cn.edu.nwsuaf.model;

/**
 * Created by huangrongchao on 2017/3/21.
 */
public class User {
//    <user name="user">
//		<property name="password">user</property>
//		<property name="schemas">USERDB</property>
//		<property name="readOnly">true</property>
//	</user>
    private String name;
    private String password;
    private String schemas;
    private boolean readOnly = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSchemas() {
        return schemas;
    }

    public void setSchemas(String schemas) {
        this.schemas = schemas;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", schemas='" + schemas + '\'' +
                ", readOnly=" + readOnly +
                '}';
    }
}
