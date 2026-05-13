const jwt = require('jsonwebtoken');

function authenticate(req, res, next) {
  const header = req.headers['authorization'];
  if (!header) return res.status(401).json({ message: 'Thiếu token xác thực' });

  const token = header.split(' ')[1];
  if (!token) return res.status(401).json({ message: 'Token không hợp lệ' });

  try {
    req.user = jwt.verify(token, process.env.JWT_SECRET);
    next();
  } catch {
    res.status(401).json({ message: 'Token hết hạn hoặc không hợp lệ' });
  }
}

function requireRole(...roles) {
  return (req, res, next) => {
    if (!roles.includes(req.user.role))
      return res.status(403).json({ message: 'Không có quyền truy cập' });
    next();
  };
}

module.exports = { authenticate, requireRole };
