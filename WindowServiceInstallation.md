# How to install StreamBaby as a Windows service #
Note:  This is not really supported and most of my development is done on Linux, so is only provided as a convenience.  I believe it works well, but...

# Details #

  * Install StreamBaby as normal
  * In the file explorer, browse to the streambaby/extra/service/win32 directory
  * On Vista, you will need to right click on "install-streambaby-service" and select run-as-administrator.  For XP simply double clicking on install-streambaby-service should suffice.
    * This will install Streambaby as an auto-start service.  It will however not be started immediately after installation (unless you reboot)
    * To start the service without reboot after you install it, for Vista right-click on start-streambaby-service and select run-as-administrator.  For XP simply double click.
  * To uninstall streambaby as a service, on Vista right-click uninstall-streambaby-service and select run-as-administrator.  For XP simply double click it.

  * Note: Windows services by default don't have access to network drives, so if any of your videos are on network drives you will have a problem.  (Same goes but worse if the streambaby app is installed on a network drive)

### Linux startup script ###
There is also a linux script that will run streambaby as daemon using standard linux daemon control parameters (start/stop/etc).  The scripts are located in the extra/service/linux32 and linux64 directories and is called simply streambaby.