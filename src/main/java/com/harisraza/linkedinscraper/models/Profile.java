package com.harisraza.linkedinscraper.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harisraza.linkedinscraper.helper.WebDriverHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "profiles")
@NoArgsConstructor
@Getter
@Setter
public class Profile {

    @Id
    private String id;
    private String name;
    private String about;
    private String experience;
    private String education;
    @Indexed(unique = true)
    private String email;
    private String link;
    private Boolean isOpenToWork;

    @DBRef
    @JsonIgnoreProperties({"posts"})
    private Search search;

    // below business logic for fetching information..
    @Transient
    @JsonIgnore
    private WebDriverHelper driverHelper;

    public Profile (String name, String email, String education, String experience, String about, boolean isOpenToWork) {
        this.name = name;
        this.email = email;
        this.education = education;
        this.experience = experience;
        this.about = about;
        this.isOpenToWork = isOpenToWork;
    }

    public Profile (WebDriverHelper driverHelper, String link) {
        this.link = link;
        this.driverHelper = driverHelper;
        driverHelper.getDriver().get(link);
    }


    /**
     * Fetches all profile information by calling individual methods.
     */
    public void fetchInformation() {
        fetchName();
        fetchAbout();
        fetchExperience();
        fetchEducation();
        fetchOpenToWork();

        // always fetch email in the last.
        fetchEmail();
    }

    /**
     * Fetch the profile name.
     *
     * @return The profile name or "Unable to get profile name" if not found.
     */
    @JsonIgnore
    private String fetchName() {
        WebElement profileNameElement = driverHelper.getElementIfExist(By.xpath("//h1[@class='text-heading-xlarge inline t-24 v-align-middle break-words']"));
        if (profileNameElement == null) return this.name = "Unable to get profile name";

        this.name = profileNameElement.getText();
        return this.name;
    }

    /**
     * Fetch the profile description.
     *
     * @return The profile description or "Unable to get profile description" if not found.
     */
    @JsonIgnore
    public String fetchAbout() {
        WebElement descriptionElement = driverHelper.getElementIfExist(By.xpath("//div[@class='text-body-medium break-words']"));
        if(descriptionElement == null) return this.about = "Unable to get profile description";

        this.about = removeDuplicateLines(descriptionElement.getText());
        return this.about;
    }

    /**
     * fetch the profile experience.
     *
     * @return The profile experience or "Unable to get experience" if not found.
     */
    @JsonIgnore
    public String fetchExperience() {
        WebElement experienceElement = driverHelper.getElementIfExist(By.id("experience"));
        if(experienceElement == null) return this.experience = "Unable to get experience";

        this.experience = removeDuplicateLines(experienceElement.findElement(By.xpath("./parent::*")).getText());
        return this.experience;
    }

    /**
     * fetch the profile education.
     *
     * @return The profile education or "Unable to get education" if not found.
     */
    @JsonIgnore
    public String fetchEducation() {
        WebElement educationElement = driverHelper.getElementIfExist(By.id("education"));
        if(educationElement == null) return "Unable to get education";

        this.education = removeDuplicateLines(educationElement.findElement(By.xpath("./parent::*")).getText());
        return this.education;
    }

    /**
     * fetch the profile email
     * @return The profile email or "Unable to get email" if not found.
     */
    @JsonIgnore
    public String fetchEmail() {
        driverHelper.getDriver().get(fetchProfileLink() + "overlay/contact-info/");

        WebElement emailElement = driverHelper.getElementIfExist(By.xpath("//section[@class='pv-contact-info__contact-type ci-email']/div/a"));
        if (emailElement != null) return this.email = emailElement.getText();
        return this.email = "Unable to get email address.";
    }

    /**
     * Checks if the profile is open to work.
     *
     * @return True if open to work, false otherwise.
     */
    @JsonIgnore
    public Boolean fetchOpenToWork() {
        WebElement isOpenToWork = driverHelper.getElementIfExist(By.xpath("//main[@class='scaffold-layout__main']/section/section/div"));
        return this.isOpenToWork = isOpenToWork != null;
    }

    /**
     * fetch the profile link.
     *
     * @return The profile link.
     */
    @JsonIgnore
    public String fetchProfileLink() {
        return driverHelper.getDriver().getCurrentUrl();
    }

    /**
     * Removes duplicate line from the given string.
     *
     * @param str The input string.
     * @return The string with duplicate words removed.
     */
    @JsonIgnore
    private static String removeDuplicateLines(String str) {
        if (str == null) return "";

        StringBuilder newStr = new StringBuilder();
        String[] lines = str.split("\n");
        Set<String> uniqueLines = new HashSet<>();

        for (String line : lines) {
            if (uniqueLines.add(line.trim())) {
                newStr.append(line).append("\n");
            }
        }

        return newStr.toString().trim();
    }

}
