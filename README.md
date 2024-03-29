# VMware has ended active development of this project, this repository will no longer be updated.
# Content Library API Samples

The Content Library is a core component that is part of the vCenter Server.
It is a new addition to the vSphere 6.0 release.<br/>

The API samples published here are accompanied by [the blogs published by the Content Library Team](http://blogs.vmware.com/developer/2015/05/content-library-blog-series.html),
which explain the use of Content Library APIs and various concepts introduced by the Content Library Service.

This document will go through the steps required:

- to quickly run a Content Library API sample
- to setup a development environment to facilitate development and exploration using the Content Library APIs


## Prerequisites

Following instructions are for the Mac OS X, but are applicable to other platforms as well (with minimal platform specific changes).

### Required
- VMware vCenter Server 6.0 installed.
- [VMware vCloud Suite SDK 6.0 for Java](https://developercenter.vmware.com/web/sdk/60/vcloudsuite-java) downloaded and extracted to a local directory.<br/> These samples are based on the following version of the SDK.<pre>VMware-vCloud-Suite-SDK-Java-6.0.0-2561089.zip
File size:55 MB
File type:zip
Release Date:2015-03-12
Build Number:2561089</pre>
- a JDK (>=1.7).
- The environment variable `JAVA_HOME` must be defined.<br/><pre>
$ export JAVA\_HOME="/Library/Java/JavaVirtualMachines/jdk1.7.0\_76.jdk/Contents/Home"
</pre>

### Optional
- `git` - good to have, but even otherwise you can download the source-code directly.


## For the impatient
### Getting the source code for Content Library API Samples and building it
- You can get the sample code by cloning this repository, or by downloading the sources directly.<br/><pre>
$ mkdir ~/my-projects
$ cd ~/my-projects
$ git clone https://github.com/vmware/content-library-api-samples.git
$ cd content-library-api-samples
</pre>

-  We need the libraries from the SDK to build the source code. Let's assume that the SDK is downloaded in `~/Downloads` directory. The extracted contents will be placed in `VMware-vCloud-Suite-SDK-Java` directory by default.<br/><pre>
$ cd ~/Downloads
$ unzip VMware-vCloud-Suite-SDK-Java-6.0.0-2561089.zip
</pre>

- Let's copy the libraries from the directory where we have extracted the SDK.<br/><pre>
$ cp -r ~/Downloads/VMware-vCloud-Suite-SDK-Java/client/lib ~/my-projects/content-library-api-samples
</pre>

- Let's build the samples and run the executor script generated by `gradle`.<br/>The code samples use `gradle` for its build system. <br/>You don't need `gradle` to be installed since `gradlew` does it for you.<br/><pre>
$ cd ~/my-projects/content-library-api-samples
$ ./gradlew installApp
</pre>

### Running a simple Content Library API Sample
- Let's run the generated executor script which is configured to run the sample from `LibraryCount.java`.<br/>
This sample connects to the provided vCenter Server instance, and lists the number of content libraries available in that instance.<pre>
$ cd ~/my-projects/content-library-api-samples
$ ./build/install/content-library-api-samples/bin/content-library-api-samples
Enter the hostname/IP for the vCenter SSO Server: vcenter.example.com
Enter SSO username: administrator@vsphere.local
Enter SSO password:
The number of libraries in this system is: 0
$
</pre>

- <strong>Congratulations!</strong> You just ran a very basic Content Library API sample successfully!<br/>Head over to the [Development Setup](#development-setup) section if you want to get your hands dirty.

- To run other samples, use of an IDE is recommended. However, following is an option to run those directly from the command line<br/><pre>
$ cd ~/my-projects/content-library-api-samples
$ java -cp "./build/libs/content-library-api-samples-1.0.jar:lib/\*" com.vmware.content.samples.LibraryCount
Enter the hostname/IP for the vCenter SSO Server: vcenter.example.com
Enter SSO username: administrator@vsphere.local
Enter SSO password:
The number of libraries in this system is: 0
</pre>
All samples have a `main` method, and reside in the [com.vmware.content.samples](src/main/java/com/vmware/content/samples/) package in this repository.<br/>
To run any sample, you could use the above pattern. <br/><pre>
$ cd ~/my-projects/content-library-api-samples
$ java -cp "./build/libs/content-library-api-samples-1.0.jar:lib/\*" com.vmware.content.samples.&lt;SAMPLE\_CLASS\_NAME&gt;
</pre>

## Development setup

### Prerequisites
In addition to the [requirements](#prerequisites) from the previous section, we will need:

- a decent IDE (Eclipse, IntelliJ IDEA, etc.). <br/>

This documentation is based on Eclipse, but the steps shouldn't be very different for other IDEs.


### Setting up your development environment
- `gradle` comes with a task to generate an Eclipse project and classpath files, so we will use that to speed up the import. <pre>
$ cd ~/my-projects/content-library-api-samples
$ ./gradlew eclipse
</pre>
This will generate the project configuration files for Eclipse in the same directory.

- Launch your Eclipse and import the `content-library-api-samples` directory as an Eclipse project.

- Once imported, your workspace should look like this:<br/>
![Sample Eclipse Workspace](http://vmware.github.io/content-library-api-samples/screenshots/sample-eclipse-workspace-v2.png "Sample Eclipse Workspace")

### Running the Content Library API samples
- To run the sample of your choice,  select and right click on the API sample in the Project Explorer view, and choose `Run As > Java Application` from the context menu.

### Using Content Library APIs
- This setup is what you need to make use of the Content Library APIs for your requirements.
- Feel free to explore Content Library APIs on your own.
-  And follow [the blogs published by the Content Library Team](https://blogs.vmware.com/developer/2015/05/content-library-blog-series.html) which explain the concepts and APIs exposed by the Content Library Service.

## Resources
- To be able to run the samples with ease, we have included sample `ISO` and `OVF` files to be used for uploads.
- However, these are not real `ISO` or `OVF` files. These are plain-text files and contain fake data. These are included for convenience only.
- It is recommended to use real `ISO` and `OVF` files to run the workflows and realize all the benefits exposed by the Content Library Service and VMware Virtual Infrastructure.



### Sample `OVF`
 - HTTP URL to be used when importing `OVF` to a library: <br/>[http://vmware.github.io/content-library-api-samples/input/sample-ovf/descriptor.ovf](http://vmware.github.io/content-library-api-samples/input/sample-ovf/descriptor.ovf) (891 bytes)
 - To upload `OVF` from a local storage to a library, please download `OVF` and its referenced `VMDK` files from following URLs in the same directory on the local storage
     - [Download `OVF` from here](http://vmware.github.io/content-library-api-samples/input/sample-ovf/descriptor.ovf) (891 bytes)
     - [Download `VMDK` for the above `OVF` from here](http://vmware.github.io/content-library-api-samples/input/sample-ovf/only-for-demo.vmdk) (68 bytes)

### Sample `ISO`
 - HTTP URL to be used when importing `ISO` to a library: <br/>[http://vmware.github.io/content-library-api-samples/input/sample-iso/only-for-demo.iso](http://vmware.github.io/content-library-api-samples/input/sample-iso/only-for-demo.iso)
 - To upload `ISO` from a local storage to a library, please download the `ISO` file from the above URL.

