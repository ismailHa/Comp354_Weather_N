# 354 project: 

## UI

- [x] Splash
    - [x] prompt to go to settings if not set up
    - [x] Otherwise, greet user and go to main page

- [x] Settings page
    - [x] About Me:
        - [x] Name   (str)
        - [x] Age    (NumberPicker)
        - [x] Weight (NumberPicker)
        - [x] Height (NumberPicker)
        - [ ] Picture?
    - [x] Services
        - [x] Dark Sky API key
        - [?] Endomondo API key
        - [ ] Login. Maybe Google Drive integration?
    - [x] Location
        - [x] Allow GPS
        - [x] Override provided GPS value
    - [x] About page
        - [x] Attribution for art assets
        - [x] Add app title
        - [x] Add app version
        - [x] Add explanation of app functionality
        - [-] Add our names + student numbers

- [-] Main Activity
    - [-] Floating Action Button to add a workout
    - [-] Today CardView
        - [x] Show today's weather textually
        - [x] Warn for chance of precipitation
        - [x] Warn for high wind speed
        - [x] Update weather image depending on scrape result
    - [-] Stats CardView    
        - [ ] Track:
            - [ ] km biked
            - [ ] temperature
            - [ ] precipitation
            - [ ] wind speed
        - [ ] Change GraphView to selected stat type when triggered by Spinner
        - [-] Show 7-day average of selected stat on bottom
        - [ ] Show 7-day low of selected stat on bottom
        - [ ] Show 7-day high of selected stat on bottom
    - [ ] You CardView
        - [ ] Display username
        - [ ] Show stats about user:
            - [ ] Number of days in streak
            - [ ] Cumulative biked distance
            - [ ] Average biked distance daily
            
- [ ] AddWorkoutActivity
    - [ ] Start time
    - [ ] End time
    - [ ] Distance
    - [ ] Calories
    - [ ] Share to social?
 
## Backend

### Weather Scraping

- [x] Make request to DS API
- [-] Handle errors for wrong API key or no network access
- [x] Send data to proper data parser

### Data Storage

- [x] Implement local file storage and keep JSON data that's downloaded
- [ ] Avoid doubling up data if we already have a today entry
- [x] Google Drive?

### Data Parsing 

- [x] Figure out which other keys need to be parsed by the JSON reader
- [x] Implement required keys in DSWeatherKey

## Overall            
 
- [x] At least 20 data points


api key: https://api.darksky.net/forecast/ac2534b55814c01e69cbc494fe1a49b5/37.8267,-122.4233
