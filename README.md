Aperte Workflow
-----------------

Aperte Workflow is a compilation of well-known, stable and mature frameworks into a complete BPM solution developed by
[BlueSoft sp. z o.o.](http://www.bluesoft.net.pl/) - Polish independent software vendor. Project home page is available
at [http://www.aperteworkflow.org/](http://www.aperteworkflow.org/).

If you want to see Aperte Workflow in action, 
there is a live demo available at [https://demo.aperteworkflow.org](https://demo.aperteworkflow.org). 

Installation bundle
-------------------
Instead of building Aperte Workflow from source, you can download installation packages from sourceforge repository: [http://sourceforge.net/projects/aperteworkflow/files/](http://sourceforge.net/projects/aperteworkflow/files/).

For a complete installation guide, tutorials and documentation, please visit the [Aperte Workflow Wiki](https://github.com/bluesoft-rnd/aperte-workflow-core/wiki).

Issues should be reported to GitHub project Issue Tracker: [https://github.com/bluesoft-rnd/aperte-workflow-core/issues](https://github.com/bluesoft-rnd/aperte-workflow-core/issues).

Key features
------------

The key features of the framework are:

* Runs on Liferay 6.x
* Support for various BPM implementations (jBPM4, jBPM5, Activiti, ...)
* Plugin support backed by OSGi service platform
* Easily extendable Relational Data Model
* Rich User Interface
* Visual process editor
* Enterprise integration backed by embedded ESB

The framework is an integration of several Open Source technologies:

* User Interface is implemented on Liferay using Vaadin framework.
* Processes can be run using a selected BPM implementation (e.g. jBPM or Activiti)
* Plugins are managed using OSGi R4 service platform (Apache Felix)
* Integration is supported by Mule ESB
* Data model is supported by Hibernate ORM

Such combination of technologies allows to build process implementation reusing existing components - provided as simple,
sometimes even process-agnostic plugins. Such plugins can provide any functionality in the solution - build part of
the user interface, start or progress processes from an email, invoke Mule ESB service, provide language localization
(i18n), etc. - while still keeping the core of the system clean and simple. Use of well-known and business-proven
Open Source technologies, guarantees the stability and maturity of the solution from the start. Also,
it allows the users of these technologies to quickly adapt to and use the framework.

For more information, please visit 
[http://www.aperteworkflow.org/](http://www.aperteworkflow.org/)

You can also see the Aperte Workflow in action: [https://demo.aperteworkflow.org](https://demo.aperteworkflow.org).

Note: as the product code name (coming way back from 2009) was "Process Tool", there might be some references in code to that name.