package net.easimer.dcctl

class LocalCommandSource (var data : IConfigData?) : ICameraCommandSource {
    override fun take(): CameraControllerCommand {
        val d = data

        return when(d) {
            null -> {
                CameraControllerCommand(true, null)
            }
            else -> {
                data = null
                CameraControllerCommand(false, d)
            }
        }
    }
}