Liam Howes
5880331
lh15fh@brocku.ca
WeatherApp Final Mobile Computing Project

Created For Use on Google Pixel 3.
Minimum API 23 (recommended: API 30).


Folders:
	You can create and delete folders to store weather locations in.
	Deleting a folder will delete all of it's stored locations and corresponding weather info.

Add Location:
	Location search accepts city name, or city name,country code(comma, no spaces).
		E.g. search: "London"
	or	E.g. search: "London,uk"

	Country codes can be found listed here for reference: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
	But...you probably don't to do that lol so just search city name. 
	the app handles FileNotFoundException so if you enter an invalid city, it will just tell you. 

Delete Location:
	Just select the locations, from the folder that you're currently viewing, that you want to delete.

Refresh Button:
	On main screen, pressing the refresh button will update the weather info for all the locations in the folder 
	you are currently viewing. This is to prevent (or discourage) the user from updating too often since
	I'm limited to 60 API calls per minute with my free OpenWeather subscription. Updating all locations
	at once could easily lead to the API key becoming invalid if OpenWeather suspends my account.
	
	You can see when the weather was last updated at the bottom right corner of each location card.