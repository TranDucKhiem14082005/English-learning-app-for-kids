package khiem.it.tinyenglish.model;

public class UserProfile {
    private String uid;
    private String username;
    private String email;
    private String gender;
    private Long age;
    private String birthDate;

    public UserProfile() {
    }

    public UserProfile(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }

    public UserProfile(String uid, String username, String email, String gender, Long age, String birthDate) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.birthDate = birthDate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}
