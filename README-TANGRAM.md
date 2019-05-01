To build a new release:

* Change the ``activiti.version`` value to the new version in the ``distro/build.xml`` file.

* Change all the ``5.17-TANGRAM-X`` ocurrences in the pom.xml files from all modules to the new version. For example:

   $ find . -name pom.xml -exec rpl 5.17-TANGRAM-5 5.17-TANGRAM-6 {} \; -print
   $ find . -name build.xml -exec rpl 5.17-TANGRAM-5 5.17-TANGRAM-6 {} \; -print

* Install the ``tangram-addons`` package:

   $ cd modules/tangram-addons/
   $ mvn install

* Build the distro:

   $ cd distro/
   $ ./build_distro.sh
