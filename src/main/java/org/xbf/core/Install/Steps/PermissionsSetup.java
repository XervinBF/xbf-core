package org.xbf.core.Install.Steps;

import org.xbf.core.Install.InstallerStage;
import org.xbf.core.Install.SetupStage;
import org.xbf.core.Models.Permissions.PGroup;
import org.xbf.core.Models.Permissions.PGroupPermission;
import org.xbf.core.Utils.Map.FastMap;

@InstallerStage(stageId=100, stageName="Setup Admin Group")
public class PermissionsSetup implements SetupStage {

	@Override
	public boolean executeStep() {
		PGroup grp = new PGroup();
		grp.gId = "admin";
		grp.displayName="Admin";
		PGroupPermission perm = new PGroupPermission();
		perm.keyIndex = "*";
		perm.value = true;
		grp.permissions.add(perm);
		
		if(!PGroup.getSmartTable().hasWithQuery(new FastMap<String, String>().add("gId", "admin"))) {
			PGroup.getSmartTable().set(grp);
		}
		return true;
	}

}
