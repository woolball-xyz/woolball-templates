import express from 'express';
import multer from 'multer';

const app = express();
const port = process.env.PORT || 3000;
const upload = multer();

const BASE_URL = "https://api.woolball.xyz/v1";
const API_KEY = "{{API_KEY}}";
const HEADERS = { 'Authorization': `Bearer ${API_KEY}` };

// Middleware for parsing JSON and raw body
app.use(express.json());
// Accept various audio/video formats
app.use(express.raw({
    type: [
        'audio/*',    // All audio formats (mp3, wav, ogg, etc)
        'video/*',    // All video formats (mp4, webm, etc)
        'application/octet-stream' // Binary data
    ],
    limit: '450mb'
}));

// Routes
app.post('/speech-to-text', upload.single('file'), async (req, res) => {
    try {
        const { language = 'pt', returnTimestamps = false, webvtt = false, model = 'onnx-community/whisper-large-v3-turbo_timestamped', url } = req.body;
            
        const requestUrl = `${BASE_URL}/speech-to-text`;

        const formData = new FormData();
        formData.append('model', model);
        formData.append('language', language);
        formData.append('returnTimestamps', returnTimestamps);
        formData.append('webvtt', webvtt);

        let response;
        if (url) {
            formData.append('url', url);
            response = await fetch(requestUrl, {
                method: 'POST',
                headers: HEADERS,
                body: formData
            });
        } else if (req.file) {
            formData.append('file', new Blob([req.file.buffer]), req.file.originalname);
            response = await fetch(requestUrl, {
                method: 'POST',
                headers: HEADERS,
                body: formData
            });
        } else {
            return res.status(400).json({ error: 'No audio/video file or URL provided' });
        }

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        res.json(data);
    } catch (error) {
        console.error('Error:', error.message);
        res.status(500).json({ error: error.message });
    }
});

app.get('/speech-to-text-models', async (req, res) => {
    try {
        const response = await fetch(`${BASE_URL}/speech-to-text-models`, {
            headers: HEADERS
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        res.json(data);
    } catch (error) {
        console.error('Error:', error.message);
        res.status(500).json({ error: error.message });
    }
});

// Start server
app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
