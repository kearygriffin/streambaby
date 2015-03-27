#push configuration for Streambaby

# Configuring you Streambaby.ini for push #

To activate "push", simply add the following entries in streambaby.ini:
```
tivo.username=yourusername@somewhere.com
tivo.password=yourpassword
```

Where yourusername@somewhere.com and yourpassword are the username and password you use to login to your account at tivo.com

Streambaby should auto-detect all of the TiVo's on the network and allow you to push videos to them. When selection a video from the selection screen, you should see the "Push" option in addition to the standard play/resume play options. If you have multiple TiVo's you can use the left/right arrows to toggle between them.

For a more advanced configuration (or if the auto-discovery does not work for some reason) you can individual designate some/all of the TiVo's:
```
tivo.1=NameOfThisTivo
tivo.1.username=username for tivo.com
tivo.1.password=password for tivo.com
tivo.1.tsn=serial number of the tivo without dashes/spaces
tivo.2.=xxxx
```