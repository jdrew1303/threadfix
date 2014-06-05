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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class RolesIndexPage extends BasePage {

	private WebElement createNewRoleLink;
	
	private List<WebElement> names = new ArrayList<>();
	private List<WebElement> editLinks = new ArrayList<>();
//	private List<WebElement> deleteButtons = new ArrayList<WebElement>();

	public RolesIndexPage(WebDriver webdriver) {
		super(webdriver);
		createNewRoleLink = driver.findElementById("createRoleModalLink");
		
		for (int i = 0; i <= getNumRows(); i++) {
//			names.add(driver.findElementById("role" + i));
			editLinks.add(driver.findElementById("editModalLink" + i));
//			deleteButtons.add(driver.findElementById("delete" + i));
		}
	}
		
	public RoleCreatePage clickCreateRoleLink() {
		createNewRoleLink.click();
		return new RoleCreatePage(driver);
	}
	public int getNumRows() {
		List<WebElement> bodyRows = driver.findElementsByClassName("roleRow");
		if (bodyRows != null && bodyRows.size() == 1 && 
				bodyRows.get(0).getText().trim().equals("No roles found.")) {
			return 0;
		}		
		
		return bodyRows.size();
	}
	
	public int getIndex(String roleName) {
		int i = -1;
		for (int j = 0; j <= getNumRows(); j++) {
			names.add(driver.findElementById("role" + j));
		}
		for (WebElement name : names) {
			i++;
			String text = name.getText().trim();
			if (text.equals(roleName.trim())) {
				return i;
			}
		}
		return -1;
	}
	
//	public String getNameContents(int row) {
//		return names.get(row).getText();
//	}
		
	public RolesIndexPage clickDeleteButton(String roleName) {
		clickEditLink(roleName);
		sleep(500);
		driver.findElementById("deleteLink").click();
		handleAlert();
		return new RolesIndexPage(driver);
	}
	
	
	public RolesIndexPage clickCreateRole(){
		driver.findElementById("createRoleModalLink").click();
		waitForElement(driver.findElementById("submit"));
		return new RolesIndexPage(driver);
	}
	
    @Deprecated
	public RolesIndexPage setRoleName(String name,String oldName){
		if(oldName == null){
			driver.findElementsById("roleNameInput").get(getNumRows()).clear();
			driver.findElementsById("roleNameInput").get(getNumRows()).sendKeys(name);
		}else{
			driver.findElementsById("roleNameInput").get(getIndex(oldName)).clear();
			driver.findElementsById("roleNameInput").get(getIndex(oldName)).sendKeys(name);
		}
		return new RolesIndexPage(driver);
	}

    public RolesIndexPage setRoleName(String roleName) {
        driver.findElementById("roleNameInput").clear();
        driver.findElementById("roleNameInput").sendKeys(roleName);
        return this;
    }

    @Deprecated
	public RolesIndexPage clickSaveRole(String oldName){ 
		if(oldName == null){
			driver.findElementById("submit").click();
            sleep(2000);
		}else{
			driver.findElementsById("submit").get(getIndex(oldName)).click();
			sleep(1000);
		}
		return new RolesIndexPage(driver);
	}

    public RolesIndexPage clickSaveRole() {
        driver.findElementById("submit").click();
        return new RolesIndexPage(driver);
    }
	
	public RolesIndexPage clickSaveRoleInvalid(String oldName){ 
		if(oldName == null){
			driver.findElementById("submit").click();
		}else{
			driver.findElementsById("submit").get(getIndex(oldName)).click();
		}
		sleep(500);
		return new RolesIndexPage(driver);
	}
	
	public RolesIndexPage clickCloseCreateRoleModal(){
		driver.findElementByLinkText("Close").click();
		sleep(1000);
		return new RolesIndexPage(driver);
	}
	
		
	public RolesIndexPage clickEditLink(String oldName) {
		editLinks.get(getIndex(oldName)).click();
		waitForElement(driver.findElementById("submit"));
		return new RolesIndexPage(driver);
	}

	public String getDisplayNameError() {
		return driver.findElementByClassName("alert-error").getText();
	}

    public String getDupNameError() {
        return driver.findElementById("roleNameInputNameError").getText();
    }

    public String getNameError(){
		return driver.findElementById("roleNameInputRequiredError").getText();
	}
	
	public boolean getPermissionValue(String permissionName, String oldName) {
        if(oldName == null){
            return driver.findElementById("newRoleModalBody").findElement(By.id(permissionName + "True")).isSelected();
        }
        return driver.findElementById("editRoleModal"+(getIndex(oldName)+1)).findElement(By.id(permissionName + "True")).isSelected();
	}

    @Deprecated
	public RolesIndexPage setPermissionValue(String permissionName, boolean value,String oldName) {
		if(oldName == null){
			String target = value ? "True" : "False";
			driver.findElementById("newRoleModalBody").findElement(By.id(permissionName + target)).click();
		} else {
			String target = value ? "True" : "False";
			driver.findElementById("editRoleModal"+(getIndex(oldName)+1)).findElement(By.id(permissionName + target)).click();
		}
		
		return new RolesIndexPage(driver);
	}

    public RolesIndexPage setPermissionValue(String permissonValue, boolean value) {
        if (value) {
            driver.findElementById("can" + permissonValue + "True").click();
        } else {
            driver.findElementById("can" + permissonValue + "False").click();
        }

        return this;
    }
	
	public RolesIndexPage clickCloseModal(){
        waitForElement(driver.findElementByClassName("modal-footer").findElement(By.className("btn")));
		driver.findElementByClassName("modal-footer").findElement(By.className("btn")).click();
		return new RolesIndexPage(driver);
	}
	
	public boolean isCreateValidationPresent(String role){
		return driver.findElementByClassName("alert-success").getText().contains("Successfully created role " + role);
	}
	
	public boolean isEditValidationPresent(String role){
		return driver.findElementByClassName("alert-success").getText().contains("Successfully edited role " + role);
	}
	
	public boolean isDeleteValidationPresent(String role){
		return driver.findElementByClassName("alert-success").getText().contains("Role deletion was successful for Role " + role);
	}
	
	public boolean isNamePresent(String name){
		return getIndex(name) != -1;
	}
}
