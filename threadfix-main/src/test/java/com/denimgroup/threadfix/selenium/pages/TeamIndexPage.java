////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2014 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.selenium.pages;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.denimgroup.threadfix.selenium.tests.TeamIndexCache;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;


public class TeamIndexPage extends BasePage {

    private List<WebElement> apps = new ArrayList<>();

    public int modalNum;
    public String appModalId;

    public TeamIndexPage(WebDriver webdriver) {
        super(webdriver);
        modalNum = 0;
        appModalId = "";
    }

    public int getNumTeamRows() {
        if (!(driver.findElementById("teamTable").getText().equals("Add Team"))) {
            return driver.findElementsByClassName("pointer").size();
        }
        return 0;
    }

    public int getNumAppRows(Integer index) {
        if (!(driver.findElementById("teamAppTableDiv" + (index))
                .getText().contains("No applications found."))) {
            return driver.findElementById("teamAppTable" + (index)).findElements(By.className("app-row")).size();
        }
        return 0;
    }

    public int getIndexSlow(String teamName) {
        System.out.println("Using slow map!");
        int i = -1;
        List<WebElement> names = new ArrayList<WebElement>();
        for (int j = 1; j <= getNumTeamRows(); j++) {
            names.add(driver.findElementById("teamName" + j));
        }
        for (WebElement name : names) {
            i++;
            String text = name.getText().trim();
            if (text.equals(teamName.trim())) {
                return i;
            }
        }
        names = new ArrayList<WebElement>();
        for (int j = 1; j <= getNumTeamRows(); j++) {
            names.add(driver.findElementById("teamName" + j));
        }
        for (WebElement name : names) {
            i++;
            String text = name.getText().trim();
            if (text.equals(teamName.trim())) {
                return i;
            }
        }
        return -1;
    }

    @Deprecated
    public Map<String, Integer> getTeamToIndexMap() {
        System.out.println("Map!");
        Map<String, Integer> teamIndexMap = new HashMap<>();

        for (int j = 1; j <= getNumTeamRows(); j++) {
            WebElement element = driver.findElementById("teamName" + j);
            teamIndexMap.put(element.getText(), j);
        }

        return teamIndexMap;
    }

    public int getIndex(String teamName) {
        TeamIndexCache cache = TeamIndexCache.getCache();
        if (cache.getIndex(teamName) < 0) {
            return getIndexSlow(teamName) + 1;
        }
        return cache.getIndex(teamName);
    }

    public int getAppIndex(String appName){
        int i = -1;
        for(WebElement app : apps){
            i++;
            String text = app.getText().trim();
            if(text.equals(appName.trim())){
                return i + 1;
            }
        }
        return -1;
    }

    public TeamIndexPage clickAddTeamButton() {
        driver.findElementById("addTeamModalButton").click();
        waitForElement(driver.findElementById("myTeamModal"));
        return setPage();
    }

    public TeamIndexPage setTeamName(String name) {
        driver.findElementById("teamNameInput").clear();
        driver.findElementById("teamNameInput").sendKeys(name);
        return setPage();
    }

    public TeamIndexPage addNewTeam() {
        TeamIndexCache cache = TeamIndexCache.getCache();
        int cnt = getNumTeamRows() + 1;

        driver.findElementById("submitTeamModal").click();
        waitForElement(driver.findElementById("teamName"+cnt));
        waitForElement(driver.findElementByClassName("alert-success"));

        String teamName = driver.findElementByClassName("alert-success").getText();
        teamName = teamName.substring(7,(teamName.length()-31));

        cache.addTeamWithName(teamName);
        System.out.println("\nAdding to TeamIndexCache: " + teamName);
        cache.printList();

        sleep(1000);
        return setPage();
    }

    public TeamIndexPage addNewTeamInvalid() {
        driver.findElementById("submitTeamModal").click();
        sleep(1000);
        return setPage();
    }


    public TeamIndexPage expandTeamRowByIndex(String teamName) {
        int teamIndex = getIndex(teamName);
        driver.findElementById("teamName" + teamIndex).click();
        try{
            populateAppList(teamIndex);
        }catch(NoSuchElementException e){
            //e.printStackTrace();
        }
        return setPage();
    }

    public TeamIndexPage expandAllTeams() {
        driver.findElementById("expandAllButton").click();
        return setPage();
    }

    public TeamIndexPage collapseAllTeams() {
        driver.findElementById("collapseAllButton").click();
        return setPage();
    }

    public void populateAppList(String teamName) {
        populateAppList(getIndex(teamName));
    }

    public void populateAppList(Integer index){
        apps = new ArrayList<>();
        sleep(5000);
        if (!driver.findElementById("teamAppTable" + (index))
                .getText().contains("No applications found.")) {
            sleep(2000);
            for (int j = 0; j < getNumAppRows(index); j++) {
                apps.add(
                        driver.findElementById(("applicationLink" + (index))
                                + "-" + (j + 1)));
            }
        }
    }

    public boolean teamAddedToTable(String teamName) {
        return teamAddedToTable(getIndex(teamName));
    }

    public boolean teamAddedToTable(Integer index) {
        if (driver.findElementById("teamName" + index) == null) {
            return false;
        }
        return true;
    }

    public ApplicationDetailPage clickViewAppLink(String appName, String teamName) {
        int teamIndex = getIndex(teamName);
        populateAppList(teamIndex);
        apps.get(getAppIndex(appName) - 1).click();
        waitForElement(driver.findElementById("actionButton1"));
        return new ApplicationDetailPage(driver);
    }

    public TeamIndexPage clickAddNewApplication(String teamName) {
        appModalId = getAppModalId(teamName);
        int teamIndex = getIndex(teamName);
        driver.findElementById("addApplicationModalButton" + teamIndex).click();
        waitForElement(driver.findElementById(appModalId));
        return setPage();
    }

    public String getAppModalId(String teamName) {
        int teamIndex = getIndex(teamName);
        String appModalIdHref = driver.findElementById("addApplicationModalButton" + teamIndex).getAttribute("href");
        String appModalId = appModalIdHref.replaceAll(".*#(myAppModal[0-9]+)$","$1");
        return appModalId;
    }

    public String getAppModalIdNumber(Integer index) {
        String appModalIdHref = driver.findElementById("addApplicationModalButton" + index).getAttribute("href");
        //String appModalIdNumber = appModalIdHref.substring(46);
        String appModalIdNumber = appModalIdHref.replaceAll(".*#myAppModal([0-9]+)$","$1");
        return appModalIdNumber;
    }

    public TeamIndexPage setApplicationName(String appName, String teamName) {
        int teamIndex = getIndex(teamName);
        driver.findElementById("nameInput" + getAppModalIdNumber(teamIndex)).clear();
        driver.findElementById("nameInput" + getAppModalIdNumber(teamIndex)).sendKeys(appName);
        return setPage();
    }

    public TeamIndexPage setApplicationUrl(String url, String teamName) {
        int teamIndex = getIndex(teamName);
        driver.findElementById("urlInput" + getAppModalIdNumber(teamIndex)).clear();
        driver.findElementById("urlInput" + getAppModalIdNumber(teamIndex)).sendKeys(url);
        return setPage();
    }

    public TeamIndexPage setApplicationCritic(String critic, String teamName) {
        int teamIndex = getIndex(teamName);
        new Select(driver.findElementById("criticalityId" + getAppModalIdNumber(teamIndex)))
                .selectByVisibleText(critic);
        return setPage();
    }

    public TeamIndexPage saveApplication(String teamName) {
        int teamIndex = getIndex(teamName);
        driver.findElementById("submitAppModal" + getAppModalIdNumber(teamIndex)).click();
        sleep(6000);
        return setPage();
    }

    public TeamIndexPage saveApplicationInvalid(String teamName) {
        int teamIndex = getIndex(teamName);
        driver.findElementById("submitAppModal" + getAppModalIdNumber(teamIndex)).click();
        return new TeamIndexPage(driver);
    }


    public TeamIndexPage clickCloseAddTeamModal(){
        driver.findElementById("closeTeamModalButton").click();
        sleep(1000);
        return new TeamIndexPage(driver);
    }


    public TeamIndexPage clickCloseAddAppModal(String teamName){
        driver.findElementById(getAppModalId(teamName)).findElement(By.className("modal-footer"))
                .findElements(By.className("btn")).get(0).click();
        sleep(1000);
        return new TeamIndexPage(driver);
    }

    public TeamIndexPage addNewApplication(String teamName, String appName,String url, String critic) {
        clickAddNewApplication(teamName);
        setApplicationName(appName, teamName);
        setApplicationUrl(url, teamName);
        setApplicationCritic(critic, teamName);
        return setPage();
    }

    public String getNameErrorMessage() {
        return driver.findElementById("name.errors").getText();
    }

    public String getUrlErrorMessage() {
        return driver.findElementById("url.errors").getText();
    }

    public boolean isAppPresent(String appName) {
        return driver.findElementByLinkText(appName).isDisplayed();
    }

    public TeamIndexPage clickUploadScan(String appName,String teamName) {
        modalNum = modalNumber(teamName,appName);
        driver.findElementById("uploadScanModalLink"+(getIndex(teamName))+"-"+(getAppIndex(appName))).click();
        waitForElement(driver.findElementById("uploadScan"+modalNum));
        return setPage();
    }

    public TeamIndexPage setPage(){
        TeamIndexPage page = new TeamIndexPage(driver);
        page.modalNum = modalNum;
        page.appModalId = appModalId;
        return page;
    }

    public TeamIndexPage setFileInput(String file) {
        driver.findElementById("fileInput"+modalNum).sendKeys(file);
        return setPage();
    }

    public ApplicationDetailPage clickUploadScanButton(String teamName, String appName) {
        driver.findElementById("submitScanModal"+modalNum).click();
        try{
            waitForInvisibleElement(driver.findElementById("uploadScan"+modalNum));
        }catch(StaleElementReferenceException e){

        }
        waitForElement(driver.findElementById("nameText"));
        waitForInvisibleElement(driver.findElementByClassName("alert-success"));
        return new ApplicationDetailPage(driver);

    }

    public ApplicationDetailPage clickUploadScanButton(String teamName, String appName,int cnt) {
        driver.findElementById("submitScanModal"+modalNum).click();
        int i = 0;
        try{
            waitForInvisibleElement(driver.findElementById("uploadScan"+modalNum));
        }catch(StaleElementReferenceException e){

        }
        waitForElement(driver.findElementById("nameText"));
//		waitForInvisibleElement(driver.findElementByClassName("alert-success"));
        try{
            while(!driver.findElementById("scanTabLink").getText().contains(Integer.toString(cnt))){
                if(i==10){
                    break;
                }
                i++;
                sleep(500);
            }
        }catch(StaleElementReferenceException e){

        }catch(UnhandledAlertException e){
        }
        //waitForElement(driver.findElementById("anyid"));
        return new ApplicationDetailPage(driver);

    }


    public TeamIndexPage clickUploadScanButtonInvalid(String teamName, String appName) {
        driver.findElementById("submitScanModal"+modalNum).click();
		/*for (int i = 1; i <= driver.findElementsByClassName("right-align")
				.size(); i++)
			if (driver.findElementById("applicationLink" + i).getText()
					.equals(appName)) {
				driver.findElementById("submitScanModal" + i).click();
				break;
			}*/
        return setPage();
    }

    public boolean isTeamPresent(String teamName) {
        int teamIndex = getIndex(teamName);
        if (teamIndex > 0) {
            return (teamName.equals(driver.findElementById("teamName" + teamIndex).getText()));
        }
        return false;
    }

    public boolean isCreateValidtionPresent(String teamName) {
        return driver
                .findElementByClassName("alert-success")
                .getText()
                .contains(
                        "Team " + teamName + " has been created successfully.");
    }

    public TeamDetailPage clickViewTeamLink(String teamName) {
        driver.findElementById("organizationLink" + getIndex(teamName)).click();
        sleep(4000);
		return new TeamDetailPage(driver);
	}

	public int modalNumber(String teamName, String appName){
        int teamIndex = getIndex(teamName);
        populateAppList(teamIndex);
        int appIndex = getAppIndex(appName);

		String s = driver.findElementById("uploadScanModalLink" + teamIndex + "-" + appIndex).getAttribute("href");
		Pattern pattern = Pattern.compile("#uploadScan([0-9]+)$");
		Matcher matcher = pattern.matcher(s);
		if(matcher.find()){
			return  Integer.parseInt(matcher.group(1));
		}
		return -1;
	}
	
	public boolean isAddTeamBtnPresent(){
		return driver.findElementById("addTeamModalButton").isDisplayed();	
	}
	
	public boolean isAddTeamBtnClickable(){
		return ExpectedConditions.elementToBeClickable(By.id("addTeamModalButton")) != null;
	}

    public boolean isTeamExpanded(String teamName){
        return driver.findElementById("teamAppTableDiv" + getIndex(teamName)).isDisplayed();
    }

    public boolean areAllTeamsExpanded() {
        for (int i = 1; i <= getNumTeamRows(); i++){
            if (!(driver.findElementById("teamAppTableDiv" + i).isDisplayed())) {
                return false;
            }
        }
        return true;
    }

    public boolean areAllTeamsCollapsed() {
        for (int i = 1; i <= getNumTeamRows(); i++){
            if (driver.findElementById("teamAppTableDiv" + i).isDisplayed()) {
                return false;
            }
        }
        return true;
    }

	public boolean isExpandAllBtnPresent(){
		return driver.findElementById("expandAllButton").isDisplayed();	
	}
	
	public boolean isExpandAllBtnClickable(){
		return ExpectedConditions.elementToBeClickable(By.id("expandAllButton")) != null;
	}
	
	public boolean isCollapseAllBtnPresent(){
		return driver.findElementById("collapseAllButton").isDisplayed();	
	}
	
	public boolean isCollapseAllBtnClickable(){
		return ExpectedConditions.elementToBeClickable(By.id("collapseAllButton")) != null;
	}
	
	public boolean isAddAppBtnPresent(String teamName){
        return driver.findElementById("addApplicationModalButton" + getIndex(teamName)).isDisplayed();
	}
	
	public boolean isAddAppBtnClickable(String teamName){
		return ExpectedConditions.elementToBeClickable(
                driver.findElementById("addApplicationModalButton" + getIndex(teamName))) != null;
	}
	
	public boolean isViewTeamLinkPresent(String teamName){
		return driver.findElementById("organizationLink" + getIndex(teamName)).isDisplayed();
	}
	
	public boolean isViewTeamLinkClickable(String teamName){
		return ExpectedConditions.elementToBeClickable(
                driver.findElementById("organizationLink" + getIndex(teamName))) != null;
	}
	
	public boolean isAppLinkPresent(String appName){
		return driver.findElementByLinkText(appName).isDisplayed();
	}
	
	public boolean isAppLinkClickable(String appName){
		return ExpectedConditions.elementToBeClickable(By.linkText(appName)) != null;
	}
	
	public boolean isUploadScanPresent(String teamName, String appName){;
		return driver.findElementById("uploadScanModalLink"+(getIndex(teamName))+"-"+(getAppIndex(appName))).isDisplayed();
	}
	
	public boolean isUploadScanClickable(String teamName, String appName){
		return ExpectedConditions.elementToBeClickable(By.linkText("uploadScanModalLink"+(getIndex(teamName))+"-"+(getAppIndex(appName)))) != null;
	}

    // TODO this needs to be changed so that it will use the img id and not a path
    public boolean isGraphDisplayed(String teamName, String appName) {
        modalNum = modalNumber(teamName, appName);
        String temp = "/threadfix/jasperimage/pointInTim" + modalNum + "/img_0_0_0";
        return driver.findElementByXPath(temp).isDisplayed();
    }
	
	public boolean isAddTeamModalPresent(){
		return driver.findElementById("myTeamModal").isDisplayed();
	}
	
	public boolean isATNameFieldPresent(){
		return driver.findElementById("teamNameInput").isDisplayed();
	}
	
	public boolean isATNameFieldFunctional(){
		int limit = Integer.parseInt(driver.findElementById("teamNameInput").getAttribute("maxlength"));
		String s = getRandomString(limit+10);
		driver.findElementById("teamNameInput").sendKeys(s);
		String v = driver.findElementById("teamNameInput").getAttribute("value");
		return v.equals(s.substring(0, limit));
	}
	
	public boolean isCloseATButtonPresent(){
		return driver.findElementById("closeTeamModalButton").isDisplayed();
	}
	
	public boolean isCloseATButtonClickable(){
		return ExpectedConditions.elementToBeClickable(By.id("closeTeamModalButton")) != null;
	}
	
	public boolean isAddTeamATButtonPresent(){
		return driver.findElementById("submitTeamModal").isDisplayed();
	}
	
	public boolean isAddTeamATButtonClickable(){
		return ExpectedConditions.elementToBeClickable(By.id("submitTeamModal")) != null;
	}
	
	public boolean isAddAppModalPresent(String teamName){
		return driver.findElementById(getAppModalId(teamName)).isDisplayed();
	}
	
	public boolean isAPNameFieldPresent(String teamName){
		return  driver.findElementById("nameInput" + getAppModalIdNumber(getIndex(teamName))).isDisplayed();
	}
	
	public boolean isAPNameFieldFunctional(String teamName){
		int limit = Integer.parseInt(driver.findElementById("nameInput" + getAppModalIdNumber(getIndex(teamName)))
                .getAttribute("maxlength"));
		
		String s = getRandomString(limit+10);
//		System.out.println(teamName + " : " + getIndex(teamName));
        driver.findElementById("nameInput" + getAppModalIdNumber(getIndex(teamName))).sendKeys(s);
		String v = driver.findElementById("nameInput" + getAppModalIdNumber(getIndex(teamName))).getAttribute("value");
		return v.equals(s.substring(0, limit));
	}
	
	public boolean isURLFieldPresent(String teamName){
        return driver.findElementById("urlInput" + getAppModalIdNumber(getIndex(teamName))).isDisplayed();
	}
	
	public boolean isURlFieldFunctional(String teamName){
		int limit = Integer.parseInt(driver.findElementById("urlInput" + getAppModalIdNumber(getIndex(teamName)))
                .getAttribute("maxlength"));
		
		String s = getRandomString(limit+10);
        driver.findElementById("urlInput" + getAppModalIdNumber(getIndex(teamName))).sendKeys(s);
		String v = driver.findElementById("urlInput" + getAppModalIdNumber(getIndex(teamName))).getAttribute("value");
		return v.equals(s.substring(0, limit));
	}
	
	public boolean isAPIDFieldPresent(String teamName){
        return driver.findElementById("uniqueIdInput" + getAppModalIdNumber(getIndex(teamName))).isDisplayed();
	}
	
	public boolean isAPIDFieldFunctional(String teamName){
		int limit = Integer.parseInt(driver.findElementById("uniqueIdInput" + getAppModalIdNumber(getIndex(teamName)))
                .getAttribute("maxlength"));
		
		String s = getRandomString(limit+10);
        driver.findElementById("uniqueIdInput" + getAppModalIdNumber(getIndex(teamName))).sendKeys(s);
		String v = driver.findElementById("uniqueIdInput" + getAppModalIdNumber(getIndex(teamName))).getAttribute("value");
		return v.equals(s.substring(0, limit));
	}
	
	public boolean isAPCriticalityPresent(String teamName){
        return driver.findElementById("criticalityId" + getAppModalIdNumber(getIndex(teamName))).isDisplayed();
	}
	
	public boolean isAPCriticalityCorrect(String teamName){
//		Select sel = new Select(driver.findElementById(getAppModalId(teamName)).findElement(By.id("criticalityId")));
//		for(int i=0; i<sel.getOptions().size(); i++)
//			System.out.println("option"+i+" "+sel.getOptions().get(i));
//		return sel.getOptions().contains("Low") && sel.getOptions().contains("Medium") && sel.getOptions().contains("High") && sel.getOptions().contains("Critical");
        //TODO
        return true;
    }

    public boolean isCloseAPButtonPresent(String teamName){
        return driver.findElementById(getAppModalId(teamName))
                .findElement(By.className("modal-footer"))
                .findElements(By.className("btn")).get(0).isDisplayed();
    }

    @SuppressWarnings("static-access")
    public boolean isCloseAPButtonClickable(String teamName){
        driver.findElementById(getAppModalId(teamName))
                .findElement(By.className("modal-footer"))
                .findElements(By.className("btn")).get(0);
        return ExpectedConditions.elementToBeClickable(By.id(getAppModalId(teamName)).className("modal-footer").className("btn")) != null;
    }

    public boolean isAddTeamAPButtonPresent(String teamName){
        return driver.findElementById(getAppModalId(teamName)).isDisplayed();
    }

    public boolean isAddTeamAPButtonClickable(String teamName){
        return ExpectedConditions.elementToBeClickable(By.id(getAppModalId(teamName))) != null;
    }

    public boolean teamVulnerabilitiesFiltered(String teamName, String level, String expected) {
        String temp = driver.findElementById("num" + level + "Vulns" + getIndex(teamName)).getText();
        return temp.equals(expected);
    }

    // TODO (redo) possibly with tags
    public boolean applicationVulnerabilitiesFiltered(String teamName, String appName, String level,String expected) {
        return getApplicationSpecificVulnerability(teamName, appName, level).equals(expected);
    }

    public String getApplicationSpecificVulnerability(String teamName, String appName, String level) {
        populateAppList(teamName);
        int appIndex = getAppIndex(appName);
        WebElement appTable = driver.findElementById("teamAppTable" + getIndex(teamName));

        List<WebElement> rows = appTable.findElements(By.className("app-row"));
        WebElement cell = rows.get(appIndex - 1).findElement(By.id("num" + level + "Vulns" + getIndex(teamName)));

        return cell.getText();
    }

    @Deprecated
    public boolean severityChanged(String teamName, String level, String expected) {
        return driver.findElementById("num" + level + "Vulns" + getIndex(teamName)).getText().equals(expected);
    }

}
