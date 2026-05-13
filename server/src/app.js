require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();

app.use(cors());
app.use(express.json());

// Routes
app.use('/api/auth',     require('./routes/auth.routes'));
app.use('/api/users',    require('./routes/user.routes'));
app.use('/api/flights',  require('./routes/flight.routes'));
app.use('/api/airports', require('./routes/airport.routes'));
app.use('/api/airplanes', require('./routes/airplane.routes'));
app.use('/api/bookings', require('./routes/booking.routes'));
app.use('/api/payments', require('./routes/payment.routes'));
app.use('/api/seats',    require('./routes/seat.routes'));
app.use('/api/staff',    require('./routes/staff.routes'));
app.use('/api/messages', require('./routes/message.routes'));

// Health check
app.get('/api/health', (req, res) => res.json({ status: 'ok' }));

// 404
app.use((req, res) => res.status(404).json({ message: 'Route không tồn tại' }));

// Global error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ message: 'Lỗi server' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));
