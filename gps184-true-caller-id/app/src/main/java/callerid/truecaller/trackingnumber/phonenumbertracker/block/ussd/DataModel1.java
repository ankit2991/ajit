package callerid.truecaller.trackingnumber.phonenumbertracker.block.ussd;

class DataModel1 {
    private final String name;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    private final String code;

    public DataModel1(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
