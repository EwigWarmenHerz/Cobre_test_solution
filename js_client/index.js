const express = require('express');
const app = express();
const port = 4000;

// Middleware para entender JSON en el body
app.use(express.json());

// Endpoint que escuchará el webhook
app.post('/webhook', (req, res) => {
    const signature = req.header('x-secret') || req.header('x-notification-Signature');
    
    console.log('--- NUEVA NOTIFICACIÓN RECIBIDA ---');
    console.log('Signature:', signature);
    console.log('Payload:', JSON.stringify(req.body, null, 2));
    
    // Responder con 200 para que tu Java sepa que llegó bien
    res.status(200).json({ status: 'received', timestamp: new Date() });
});

app.listen(port, () => {
    console.log(`Servidor de pruebas escuchando en http://localhost:${port}/webhook`);
});
