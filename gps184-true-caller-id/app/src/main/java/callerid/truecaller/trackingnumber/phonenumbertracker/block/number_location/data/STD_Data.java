package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.data;

import java.io.Serializable;

public class STD_Data implements Serializable {
    String city;
    int code;
    int id;
    int state_id;

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getState_id() {
        return this.state_id;
    }

    public void setState_id(int state_id) {
        this.state_id = state_id;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
