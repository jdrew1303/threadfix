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

package com.denimgroup.threadfix.selenium.enttests;

import com.denimgroup.threadfix.data.entities.Role;
import com.denimgroup.threadfix.EnterpriseTests;
import com.denimgroup.threadfix.selenium.pages.*;
import com.denimgroup.threadfix.selenium.tests.BaseIT;
import com.denimgroup.threadfix.selenium.utils.DatabaseUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(EnterpriseTests.class)
public class RoleEntIT extends BaseIT {

	RolesIndexPage rolesIndexPage = null;

	/**
	 * Also tests delete
	 */
	@Test
	public void testCreateRole() {
		// needs to be alphabetically before "Admin" preset role
		//String roleName = "Aa" + getRandomString(8);
        String roleName = getRandomString(8);

		RolesIndexPage rolesIndexPage = loginPage.login("user", "password")
				.clickManageRolesLink()
				.clickCreateRole()
				.setRoleName(roleName)
				.clickSaveRole();

		assertTrue("Role not added.", rolesIndexPage.isNamePresent(roleName));
		assertTrue("Validation message is Present.",rolesIndexPage.isCreateValidationPresent(roleName));
	}

    @Test
    public void testDeleteRole() {
        String roleName = getRandomString(8);

        RolesIndexPage rolesIndexPage = loginPage.login("user", "password")
                .clickManageRolesLink()
                .clickCreateRole()
                .setRoleName(roleName)
                .clickSaveRole();

        assertTrue("Role not added.", rolesIndexPage.isNamePresent(roleName));

        rolesIndexPage = rolesIndexPage.clickDeleteButton(roleName);
        assertTrue("Validation message is Present.",rolesIndexPage.isDeleteValidationPresent(roleName));
        assertFalse("Role not removed.", rolesIndexPage.isNamePresent(roleName));
    }

	@Test
	public void testEditRole() {
		String name1 = "1" + getRandomString(15);
		String name2 = "2" + getRandomString(15);

		RolesIndexPage rolesIndexPage = loginPage.login("user", "password")
				.clickManageRolesLink()
				.clickCreateRole()
				.setRoleName(name1,null)
				.clickSaveRole(null)
				.clickEditLink(name1)
				.clickSaveRole(name1);

		assertTrue("Role not added.", rolesIndexPage.isNamePresent(name1));
		assertTrue("Validation message is Present.",rolesIndexPage.isEditValidationPresent(name1));
		
		rolesIndexPage = rolesIndexPage.clickEditLink(name1)
				.setRoleName(name2,name1)
				.clickSaveRole(name1);
		
		assertTrue("Role not Edited Correctly.", rolesIndexPage.isNamePresent(name2));
		assertTrue("Validation message is Present.",rolesIndexPage.isEditValidationPresent(name2));
		
		rolesIndexPage = rolesIndexPage.clickDeleteButton(name2);

		assertTrue("Validation message is Present.",rolesIndexPage.isDeleteValidationPresent(name2));
		assertFalse("Role not removed.", rolesIndexPage.isNamePresent(name2));

	}


    @Test
    public void testCreateRoleValidation() {
        String whiteSpaceName = "     ";

        // Test whitespace
        RolesIndexPage rolesIndexPage = loginPage.login("user", "password")
                .clickManageRolesLink()
                .clickCreateRole()
                .setRoleName(whiteSpaceName,null)
                .clickSaveRole(null);

        assertTrue("Blank field error didn't show correctly.",
                rolesIndexPage.getNameError().contains("Name is required."));
    }


    @Test
	public void testCreateRoleDupicateValidation() {

		String name1 = "testNameDuplication";
        String name2 = "testNameDuplication";

		// Test duplicates
		RolesIndexPage rolesIndexPage = loginPage.login("user", "password").clickOrganizationHeaderLink()
                .clickManageRolesLink()
				.clickCreateRole()
				.setRoleName(name1,null)
				.clickSaveRole(null)
				.clickManageRolesLink()
				.clickCreateRole()
				.setRoleName(name2,null)
				.clickSaveRole(null);

		assertTrue("Duplicate name error did not show correctly.",
				rolesIndexPage.getDupNameError().contains("That name is already taken."));

		rolesIndexPage = rolesIndexPage.clickCloseCreateRoleModal();
	}

	@Test
	public void addApplicationOnlyRole(){
		String roleName = "appOnly" + getRandomString(10);
		String userName = getRandomString(10);
		String password = getRandomString(15);
		String teamName = getRandomString(10);
		String appName = getRandomString(10);

        DatabaseUtils.createTeam(teamName);
        DatabaseUtils.createApplication(teamName, appName);

		TeamIndexPage teamIndexPage = loginPage.login("user", "password")
					 .clickOrganizationHeaderLink()
					 .clickManageRolesLink()
					 .clickCreateRole()
					 .setRoleName(roleName)
					 .setPermissionValue("ManageApplications", true)
					 .clickSaveRole()
                     .clickOrganizationHeaderLink();

        teamIndexPage.clickManageUsersLink()
                     .clickAddUserLink()
                     .enterName(userName)
                     .enterPassword(password)
                     .enterConfirmPassword(password)
                     .chooseRoleForGlobalAccess(roleName, null)
                     .clickAddNewUserBtn()
                     .logout();

		ApplicationDetailPage applicationDetailPage = loginPage.login(userName, password)
					.clickOrganizationHeaderLink()
					.expandTeamRowByName(teamName)
					.addNewApplication(teamName, appName, "", "Low")
				    .saveApplication()
					.clickOrganizationHeaderLink()
					.expandTeamRowByName(teamName)
					.clickViewAppLink(appName, teamName);

		assertTrue("new role user was not able to add an application",
                applicationDetailPage.getNameText().contains(appName));
	}

    // TODO: Enterprise is not an option in Role Permissions
    @Ignore
	@Test
	public void testSetPermissions() {
		String name = "testName" + getRandomString(10);
		
		rolesIndexPage = loginPage.login("user", "password")
				.clickManageRolesLink()
				.clickCreateRole()
				.setRoleName(name,null);
		
		for (String role : Role.ALL_PERMISSIONS) {
			assertFalse("Checkbox was set to true when it shouldn't have been.", 
					rolesIndexPage.getPermissionValue(role,null));
		}
		
		for (String role : Role.ALL_PERMISSIONS) {
			assertFalse("Checkbox was set to true when it shouldn't have been.", 
					rolesIndexPage.getPermissionValue(role,null));
			rolesIndexPage.setPermissionValue(role, true, null);
		}
		
		rolesIndexPage = rolesIndexPage.clickSaveRole(null)
									.clickEditLink(name);
		for (String role : Role.ALL_PERMISSIONS) {
			assertTrue("Role was not turned on correctly.", rolesIndexPage.getPermissionValue(role,name));
			rolesIndexPage.setPermissionValue(role, false,name);
		}
		
		rolesIndexPage = rolesIndexPage.clickSaveRole(name)
									.clickEditLink(name);
		
		for (String role : Role.ALL_PERMISSIONS) {
			assertFalse("Role was not turned off correctly.", rolesIndexPage.getPermissionValue(role,name));
		}
		rolesIndexPage = rolesIndexPage.clickSaveRole(name)
									.clickDeleteButton(name)
									.clickCreateRole()
									.setRoleName(name,null);
		
		for (String role : Role.ALL_PERMISSIONS) {
			rolesIndexPage.setPermissionValue(role, true, null);
		}
		
		rolesIndexPage = rolesIndexPage.clickSaveRole(null).clickEditLink(name);
		
		for (String role : Role.ALL_PERMISSIONS) {
			assertTrue("Role was not turned on correctly.", rolesIndexPage.getPermissionValue(role,name));
		}
		
		rolesIndexPage = rolesIndexPage.clickSaveRole(name)
				.clickDeleteButton(name);
		assertTrue("Validation message is Present.",rolesIndexPage.isDeleteValidationPresent(name));
		assertFalse("Role not removed.", rolesIndexPage.isNamePresent(name));
	}
	
	// these tests are to ensure that threadfix cannot enter a state with no users that
	// have permissions to manage users / roles / groups
    // TODO: bug filed for Read Access not an option in Roles
    @Ignore
	@Test
	public void testRemoveRolesFromUser() {
		String admin = "Administrator";
		
		 UserIndexPage userIndexPage = loginPage.login("user", "password")
				.clickManageUsersLink()
				.clickAddUserLink();

		   userIndexPage.enterName("RoleRemoval",null)
		 				.enterPassword("passwordpassword", null)
		 				.enterConfirmPassword("passwordpassword", null)
		 				.chooseRoleForGlobalAccess(admin, null)
		 				.clickAddNewUserBtn()
		 				.clickEditLink("user")
                        .enterPassword("passwordpassword", null)
                        .enterConfirmPassword("passwordpassword", null)
		 				.chooseRoleForGlobalAccess("Read Access", "user")
		 				.clickUpdateUserBtn("user");
		 
		
		rolesIndexPage = userIndexPage.clickManageRolesLink()
				.clickEditLink(admin);
		
		for (String role : Role.ALL_PERMISSIONS) {
			assertTrue("Admin role did not have all permissions.", rolesIndexPage.getPermissionValue(role,admin));
		}
		
		for (String protectedPermission : Role.PROTECTED_PERMISSIONS) {
			rolesIndexPage.setPermissionValue(protectedPermission, false,admin);
		}	
		rolesIndexPage.clickSaveRoleInvalid(admin);
		assertTrue("Protected permission was not protected correctly.", 
				rolesIndexPage.getDisplayNameError().contains("You cannot remove the Manage Users privilege from this role."));

		rolesIndexPage = rolesIndexPage.clickCloseModal()
									.clickManageRolesLink()
									.clickEditLink(admin);
		
		for (String role : Role.ALL_PERMISSIONS) {
			assertTrue("Admin role did not have all permissions.", rolesIndexPage.getPermissionValue(role,admin));
		}
		
		rolesIndexPage.clickManageUsersLink()
						.clickEditLink("user")
						.chooseRoleForGlobalAccess(admin, "user")
						.clickUpdateUserBtn("user")
						.clickDeleteButton("RoleRemoval");
		
	}

    // TODO this test will not run correctly because of bugs involved with editing user options
    @Ignore
	@Test
	public void testDeleteRoleWithUserAttached(){
		String roleName = "test" + getRandomString(10);
		String roleName2 = "test" + getRandomString(10);
		rolesIndexPage = loginPage.login("user", "password")
								.clickManageRolesLink();
		
		rolesIndexPage = rolesIndexPage.clickCreateRole()
				.setRoleName(roleName,null)
				.clickSaveRole(null)
				.clickCreateRole()
				.setRoleName(roleName2,null)
				.clickSaveRole(null)
				.clickEditLink(roleName);
		
		for (String protectedPermission : Role.ALL_PERMISSIONS) {
			rolesIndexPage = rolesIndexPage.setPermissionValue(protectedPermission, true,roleName);
		}
		
		rolesIndexPage = rolesIndexPage.clickSaveRole(roleName)
									.clickEditLink(roleName2);
		
		for (String protectedPermission : Role.ALL_PERMISSIONS) {
			rolesIndexPage = rolesIndexPage.setPermissionValue(protectedPermission, true,roleName2);
		}
		
		rolesIndexPage = rolesIndexPage.clickSaveRole(roleName2)
					.clickManageUsersLink()
					.clickEditLink("user")
					.chooseRoleForGlobalAccess(roleName, "user")
					.clickUpdateUserBtn("user")
					.clickManageRolesLink()
					.clickDeleteButton(roleName)
                    .clickManageRolesLink();

		assertTrue("Role was not removed.",rolesIndexPage.isNamePresent(roleName));
		
		rolesIndexPage = rolesIndexPage.clickManageUsersLink()
									.clickEditLink("user")
									.chooseRoleForGlobalAccess(roleName2, "user")
									.clickUpdateUserBtn("user")
									.clickManageRolesLink()
									.clickDeleteButton(roleName);
		
		assertFalse("Role was not removed.",rolesIndexPage.isNamePresent(roleName));
		
		rolesIndexPage = rolesIndexPage.clickManageUsersLink()
				.clickEditLink("user")
				.chooseRoleForGlobalAccess("Administrator", "user")
				.clickUpdateUserBtn("user")
				.clickManageRolesLink()
				.clickDeleteButton(roleName2);
		
		assertFalse("Role was not removed.",rolesIndexPage.isNamePresent(roleName2));
	}





}
