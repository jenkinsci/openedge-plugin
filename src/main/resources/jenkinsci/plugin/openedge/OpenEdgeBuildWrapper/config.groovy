package jenkinsci.plugin.openedge;
def f = namespace(lib.FormTagLib)
def installationsDefined = descriptor.installations.length != 0
def title = installationsDefined ? _("OpenEdge version") : _("No setup will be done, as no Go installations have been defined in the Jenkins system config")
f.entry(title: title) {
 if (installationsDefined) {
  select(class:"setting-input", name:".openEdgeInstall") {
   descriptor.installations.each {
    f.option(selected: it.name == instance?.openEdgeInstallation?.name, value: it.name, it.name)
   }
  }
 }
}