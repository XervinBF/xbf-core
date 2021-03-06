package org.xbf.core;

public class XVI {
	public static Version[] versions = new Version[] {
			new Version("0.0.1"        , "Krawlo"    , "2020-09-28", "- Framework base code"),
			new Version("0.0.2"        , "Krawlo"    , "2020-09-29", "- Change to xbf-core"),
			new Version("0.0.3"        , "Krawlo"    , "2020-09-29", "- Add Database provider registration"),
			new Version("0.0.4"        , "Krawlo"    , "2020-09-29", "- Better plugin depend system"),
			new Version("0.0.5"        , "Krawlo"    , "2020-09-29", "- DBProvider registration didn't work\n- Better plugin depends checking"),
			new Version("0.0.6"        , "Krawlo"    , "2020-09-29", "- DBProvider can throw exceptions"),
			new Version("0.0.7"        , "Krawlo"    , "2020-09-29", "- Make easier to implement database providers for databases that do not return a ResultSet"),
			new Version("0.0.8"        , "Krawlo"    , "2020-09-30", "- Plugin Configuration"),
			new Version("0.0.9"        , "Krawlo"    , "2020-09-30", "- Small plugin config fixes\n- Improvments in DBResult\n- Add automatic field adding to SmartTable\n- Other small fixes"),
			new Version("0.0.10"       , "Krawlo"    , "2020-10-01", "- Better language loading\n- Improved object mapping"),
			new Version("0.0.11"       , "Krawlo"    , "2020-10-02", "- Extended support for specific channels to send to"),
			new Version("0.0.12"       , "Krawlo"    , "2020-11-15", "- PluginLoading with one classloader chain\n- Change startup orders to allow for easier integration of other plugins"),
			new Version("0.0.13"       , "Krawlo"    , "2020-12-06", "- Compilation bug"),
			new Version("0.0.14"       , "Krawlo"    , "2020-12-20", "- SmartTable: Set custom key fields for tables"),
			new Version("0.1.0"        , "Krawlo"    , "2021-07-14", "- SmartTable: Better updates\n- Improved Permissions system"),

	};

	public static Version version = versions[versions.length - 1];

	public static class Version {

		public String version;
		public String name;
		public String release;
		public String changelog;

		public Version(String version, String name, String release, String changelog) {
			if(version == null)
	            throw new IllegalArgumentException("Version can not be null");
	        if(!version.matches("[0-9]+(\\.[0-9]+)*"))
	            throw new IllegalArgumentException("Invalid version format");
			this.version = version; // x.x.x
			this.name = name; // xxxxx
			this.release = release; // yyyy-MM-dd
			this.changelog = changelog; // string

		}

		public String getVersionString() {
			return name + " - " + version + " (" + release + ")";
		}

		public int compareTo(Version that) {
			if (that == null)
				return 1;
			String[] thisParts = this.version.split("\\.");
			String[] thatParts = that.version.split("\\.");
			int length = Math.max(thisParts.length, thatParts.length);
			for (int i = 0; i < length; i++) {
				int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
				int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;
				if (thisPart < thatPart)
					return -1;
				if (thisPart > thatPart)
					return 1;
			}
			return 0;
		}

		public boolean equals(Object that) {
			if (this == that)
				return true;
			if (that == null)
				return false;
			if (this.getClass() != that.getClass())
				return false;
			return this.compareTo((Version) that) == 0;
		}

	}
}
