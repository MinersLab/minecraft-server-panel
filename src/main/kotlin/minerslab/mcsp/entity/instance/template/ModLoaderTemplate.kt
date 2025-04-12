package minerslab.mcsp.entity.instance.template

interface ModLoaderTemplate<T : TemplateArgument> : InstanceTemplate<T> {
    companion object {
        const val SERVER_JAR = "server.jar"
        const val SERVER_INSTALLER_JAR = "server-installer.jar"
    }
}
