import { NextRequest, NextResponse } from 'next/server';

const BASE_URL = "https://api.woolball.xyz/v1";
const API_KEY = "{{API_KEY}}";
const HEADERS = { 'Authorization': `Bearer ${API_KEY}` };

export async function POST(request: NextRequest) {
    try {
        const contentType = request.headers.get('content-type') || '';
        const requestUrl = `${BASE_URL}/speech-to-text`;
        const formData = new FormData();

        if (contentType.includes('multipart/form-data')) {
            const originalFormData = await request.formData();
            // Forward all form fields
            for (const [key, value] of originalFormData.entries()) {
                formData.append(key, value);
            }
        } else if (contentType.includes('application/json')) {
            const jsonData = await request.json();
            if (jsonData.url) {
                formData.append('url', jsonData.url);
            }
            formData.append('model', jsonData.model || 'onnx-community/whisper-large-v3-turbo_timestamped');
            formData.append('language', jsonData.language || 'pt');
            formData.append('returnTimestamps', String(jsonData.returnTimestamps || false));
            formData.append('webvtt', String(jsonData.webvtt || false));
        } else if (
            contentType.startsWith('audio/') || 
            contentType.startsWith('video/') || 
            contentType === 'application/octet-stream'
        ) {
            const fileData = await request.arrayBuffer();
            formData.append('file', new Blob([fileData]), 'audio.mp3');
            formData.append('model', 'onnx-community/whisper-large-v3-turbo_timestamped');
            formData.append('language', 'pt');
            formData.append('returnTimestamps', 'false');
            formData.append('webvtt', 'false');
        } else {
            return NextResponse.json({ error: 'No audio/video file or URL provided' }, { status: 400 });
        }

        const response = await fetch(requestUrl, {
            method: 'POST',
            headers: HEADERS,
            body: formData
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error: any) {
        console.error('Error:', error.message);
        return NextResponse.json({ error: error.message }, { status: 500 });
    }
}

export async function GET() {
    try {
        const response = await fetch(`${BASE_URL}/speech-to-text-models`, {
            headers: HEADERS
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error: any) {
        console.error('Error:', error.message);
        return NextResponse.json({ error: error.message }, { status: 500 });
    }
}
