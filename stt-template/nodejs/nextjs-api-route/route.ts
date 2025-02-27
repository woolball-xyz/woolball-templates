import { NextRequest, NextResponse } from 'next/server';

const BASE_URL = "https://api.woolball.xyz/v1";
const API_KEY = "{{API_KEY}}";
const HEADERS = { 'Authorization': `Bearer ${API_KEY}` };

export async function POST(request: NextRequest) {
    try {
        // Handle both FormData and direct file uploads
        const contentType = request.headers.get('content-type') || '';
        let audioUrl: string | null = null;
        let audioFile: ArrayBuffer | null = null;
        let language = 'pt';
        let returnTimestamps = false;
        let webvtt = false;
        let model = 'onnx-community/whisper-large-v3-turbo_timestamped';
        let fileType: string | null = null;

        if (contentType.includes('multipart/form-data')) {
            const formData = await request.formData();
            const file = formData.get('file') as File | null;
            audioUrl = formData.get('url') as string;
            language = formData.get('language') as string || language;
            returnTimestamps = formData.get('returnTimestamps') === 'true';
            webvtt = formData.get('webvtt') === 'true';
            model = formData.get('model') as string || model;

            if (file) {
                audioFile = await file.arrayBuffer();
                fileType = file.type || 'audio/mpeg';
            }
        } else if (contentType.includes('application/json')) {
            const jsonData = await request.json();
            audioUrl = jsonData.url;
            language = jsonData.language || language;
            returnTimestamps = jsonData.returnTimestamps || returnTimestamps;
            webvtt = jsonData.webvtt || webvtt;
            model = jsonData.model || model;
        } else if (
            contentType.startsWith('audio/') || 
            contentType.startsWith('video/') || 
            contentType === 'application/octet-stream'
        ) {
            audioFile = await request.arrayBuffer();
            fileType = contentType;
        }

        const requestUrl = `${BASE_URL}/speech-to-text?model=${encodeURIComponent(model)}&language=${language}&returnTimestamps=${returnTimestamps}&webvtt=${webvtt}`;

        let response;
        if (audioUrl) {
            response = await fetch(requestUrl, {
                method: 'POST',
                headers: { ...HEADERS, 'Content-Type': 'application/json' },
                body: JSON.stringify({ url: audioUrl })
            });
        } else if (audioFile) {
            response = await fetch(requestUrl, {
                method: 'POST',
                headers: { ...HEADERS, 'Content-Type': fileType || 'audio/mpeg' },
                body: audioFile
            });
        } else {
            return NextResponse.json({ error: 'No audio/video file or URL provided' }, { status: 400 });
        }

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
