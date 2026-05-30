# Folder Properties

The Folder Properties plugin allows users with config permission to define
properties for a folder which can then be used by jobs contained
within it or in any of its sub-folders.

The aim here is to remove the need to specify the same properties over
and over again for all the jobs inside a folder.

## How to use?

To configure, just create a
[folder](https://plugins.jenkins.io/cloudbees-folder/),
go to its configuration page and add as many properties as you need
under the `Folder Properties` section.

In structures where two or more folders are nested, any property defined for a folder will be overridden by any other
property of the same name defined by one of its sub-folders.

![](docs/images/folder-properties-config.png)

## Global Configuration

Folder properties can be **globally injected** into all jobs within those folders, regardless of individual job
configuration.

When global injection is enabled, the properties defined on a folder are automatically exposed (via
environment variables) to every job in that folder and its sub-folders. For Pipeline jobs this means the values
are available in all parts of the pipeline script, including top‑level sections such as the `parameters` block,
without needing to wrap code in a specific step.

By contrast, using the `withFolderProperties` step (or `options { withFolderProperties() }` in a declarative
pipeline, as shown below) only makes the properties available inside the wrapped block or for the duration of the
pipeline run where the option is applied. This gives you more explicit control over the scope of the properties and
avoids affecting jobs that do not opt in.

As a rule of thumb, use **global injection** when you want a consistent set of shared defaults for all jobs in a
folder hierarchy (for example, common URLs, credentials IDs, or organizational settings). Use **`withFolderProperties`**
when you want per-pipeline control over when those properties are visible, or when you prefer jobs to explicitly opt
into using folder properties.

![](docs/images/folder-properties-global-config.png)

## Freestyle Jobs

Freestyle jobs must opt into the `Folder Properties` build wrapper from
the `Build Environment` section of their configuration page in order to
be able to access these properties as they would any other environment
variable.

![](docs/images/folder-properties-freestyle-config.png)

Only then will they inherit properties defined by their parent or ancestor folders —e.g. Running `echo $FOO` in a Shell build step :

![](docs/images/freestyle-example-1.png)

#### SCM Step in Freestyle Jobs

Freestyle jobs can also use folder properties to **define SCM parameters** — e.g. By defining an `SCM_URL` property pointing to the Git repository and a `BRANCH_SELECTOR` property pointing to the branch, tag or commit to be checked out:

![](docs/images/freestyle-example-scm-1.png)

Then, descendant freestyle jobs can use that either as `$SCM_URL` and `$BRANCH_SELECTOR` :

![](docs/images/freestyle-example-scm-2.png)

 or as `${SCM_URL}` and `${BRANCH_SELECTOR}` :

![](docs/images/freestyle-example-scm-3.png)

## Pipeline Jobs

Pipeline jobs can use step `withFolderProperties` to access them :

**Using folder properties in a pipeline job**

``` groovy
withFolderProperties{
    echo("Foo: ${env.FOO}")
}
```

Declarative pipeline jobs can also use the `options` directive to leverage folder properties as follows:

``` groovy
pipeline {
    agent any
    options {
        withFolderProperties()
    }
    stages {
        stage('Test') {
            steps {
                echo("Foo: ${env.FOO}")
            }
        }
    }
}
```

## Job DSL

In Job DSL scripts you can define folder properties like so :

**Job DSL example**

``` groovy
folder('my folder') {
    properties {
        folderProperties {
            properties {
                stringProperty {
                    key('FOO')
                    value('foo1')
                }
            }
        }
    }
}
```

## Authors & Contributors

* [Miguelángel Fernández Mendoza](https://github.com/mig82).
* [GongYi](https://github.com/topikachu).
* [Stefan Hirche](https://github.com/StefanHirche)
* [Deepak Gupta](https://github.com/Mr-DG-Wick)
* [Jiri Simacek](https://github.com/jsimacek)

## References

* [Site](https://plugins.jenkins.io/folder-properties/)
* [Dependencies](https://plugins.jenkins.io/folder-properties/dependencies/)
* [Javadoc](https://javadoc.jenkins.io/plugin/folder-properties/)
