const express = require('express');
const fp = require("@fingerprintjs/fingerprintjs-pro-server-api");
const app = express();
const port = 3000;

app.use(express.json());

function isLoginValid(email, password) {
    return email === 'fingerprint@example.com' && password === 'secret';
}

const client = new fp.FingerprintJsServerApiClient({
    // secret API key
    // get the real key in Fingerprint dashboard
    apiKey: 'SECRET_KEY',
    region: fp.Region.Global,
});

app.post('/', async (req, res) => {
    const { email, password, requestId } = req.body;
    if (!isLoginValid(email, password)) {
        return res.status(401).send("Email or password don't match")
    }
    // This will call fingerprint Server API
    const event = await client.getEvent(requestId);
    const isAppCloned = event.products.clonedApp.data.result;
    if(isAppCloned){
        return res.status(403).send("Cloned apps are not permitted");
    }
    res.send("Login successful")
});


// Start server
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});