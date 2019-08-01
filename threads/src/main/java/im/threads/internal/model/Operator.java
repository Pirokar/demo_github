package im.threads.internal.model;

/**
 * Created by Admin on 25.05.2017.
 */

public class Operator {

    private Long id;
    private String name;
    private String role;
    private String orgUnit;
    private Long maxThreads;
    private String photoUrl;

    private Gender gender = Gender.FEMALE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(Long maxThreads) {
        this.maxThreads = maxThreads;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(String orgUnit) {
        this.orgUnit = orgUnit;
    }

    public enum Gender {
        MALE,
        FEMALE;
    }
}
