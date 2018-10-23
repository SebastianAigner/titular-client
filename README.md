# Titular Client

**Titular** is a multiplayer online game where you and your friends look at Reddit-curated images from the platform WikiHow and try to find funny captions for it. Once everyone has put in a suggestion, everyone gets to vote on the best caption! May the funniest person earn the most points!

Titular is a project that (besides being a fun pastime with friends) exemplifies how Kotlin can be used all throughout the stack.

This is the client repository for the game. Find the server repository [here](https://github.com/SebastianAigner/titular-server).

### Technolgies Used

- React (using [Create-React-Kotlin-App](https://github.com/JetBrains/create-react-kotlin-app))
- Twemoji-Amazing
- [Animate.css](https://github.com/daneden/animate.css)
- (Bonus: [why-did-you-update](https://github.com/maicki/why-did-you-update))

## Setup and Running the App

### `npm install` or `yarn install`
This downloads the dependencies for the first time and sets the application up so that you can run it locally.

Once the installation is done, you can run some commands inside the project folder:

### `npm start` or `yarn start`

Runs the app in development mode.<br>
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload automatically when you make edits.<br>
You will see build errors and lint warnings in the console.

### `npm run build` or `yarn build`

Builds the app for production to the `build` folder.<br>
It ensures that React is bundled in production mode and the build is optimized for best performance.

The build is minified and the filenames include hashes for cache management. Your app is ready to be deployed.

## Debugging the App

You can debug the running app right in IntelliJ IDEA Ultimate using its built-in JavaScript debugger. The IDE will run a new instance of Chrome and attach a debugger to it.

Start your app by running `npm start`. Put the breakpoints in your Kotlin code.
Then select `Debug in Chrome` from the list of run/debug configurations on the top-right and click the green debug icon or press `^D` on macOS or `F9` on Windows and Linux to start debugging.

Currently, debugging is supported only in IntelliJ IDEA Ultimate 2017.3.

You can also debug your application using the developer tools in your browser.

