package cn.edu.nwsuaf.model;

/**
 * Created by huangrongchao on 2017/3/21.
 */
public class User {
    private String name;
    private String password;
    private String schemas;
    private boolean readOnly = false;
    private boolean used = false;

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

    public boolean isUsed() {
        return used;
    }

    public void setUse(boolean used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", schemas='" + schemas + '\'' +
                ", readOnly=" + readOnly +
                ", used=" + used +
                '}';
    }
}
