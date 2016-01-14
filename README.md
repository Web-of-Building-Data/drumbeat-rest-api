# drumbeat-rest-api

Pre-required softwares:
* Java SDK 1.8
* git
* maven


Step-by-step run the following commands:

1) Compiling drumbeat-jena

>git clone https://github.com/Web-of-Building-Data/drumbeat-jena.git

if you are working with feature-nam-impl branch
> git checkout feature-nam-impl

>cd !drumbeat-jena

>mvn clean install

2) Compiling drumbeat-ifc2ld

>git clone https://github.com/Web-of-Building-Data/drumbeat-ifc2ld.git

if you are working with feature-nam-impl branch
>git checkout feature-nam-impl

>cd !drumbeat-ifc2ld-parent

>mvn clean install


3) Compiling drumbeat-rest-api

>git clone https://github.com/Web-of-Building-Data/drumbeat-rest-api.git

if you are working with feature-nam-impl branch
>git checkout feature-nam-impl

>cd !drumbeat-rest-api-parent

>mvn clean install
