const express = require('express');
const fs = require('fs');
const path = require('path');
const compression = require('compression'); // Рівень 4: gzip-стиснення

const app = express();
const PORT = 3000;
const DATA_FILE = path.join(__dirname, 'data', 'data.json');

// ============================================================
// Middleware
// ============================================================

// Рівень 4: gzip-стиснення для оптимізації навантажень
app.use(compression());

app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Рівень 4: кешування статичних файлів
app.use('/static', express.static(path.join(__dirname, 'public'), {
    maxAge: '1d',
    etag: true,
}));

// ============================================================
// Допоміжні функції для роботи з даними (JSON-файл)
// ============================================================

function readData() {
    const raw = fs.readFileSync(DATA_FILE, 'utf-8');
    return JSON.parse(raw);
}

function writeData(data) {
    fs.writeFileSync(DATA_FILE, JSON.stringify(data, null, 2), 'utf-8');
}

// Простий middleware для авторизації (Рівень 3)
function authenticate(req, res, next) {
    const userId = req.headers['x-user-id'];
    if (!userId) {
        return res.status(401).json({ error: 'Необхідна авторизація' });
    }
    const data = readData();
    const user = data.users.find(u => u.id === parseInt(userId));
    if (!user) {
        return res.status(401).json({ error: 'Користувача не знайдено' });
    }
    req.user = user;
    next();
}

// Перевірка ролі адміністратора (Рівень 3)
function requireAdmin(req, res, next) {
    if (req.user.role !== 'admin') {
        return res.status(403).json({ error: 'Доступ заборонено. Потрібні права адміністратора' });
    }
    next();
}

// ============================================================
// Рівень 1 + 2: API для відео
// ============================================================

// Отримати всі відео
app.get('/api/videos', (req, res) => {
    const data = readData();
    const videos = data.videos.map(v => {
        const author = data.users.find(u => u.id === v.authorId);
        return {
            ...v,
            authorName: author ? author.username : 'Невідомий',
            channelName: author ? author.channel : 'Невідомий канал',
            commentCount: v.comments.length,
        };
    });
    res.json(videos);
});

// Отримати одне відео за ID
app.get('/api/videos/:id', (req, res) => {
    const data = readData();
    const video = data.videos.find(v => v.id === parseInt(req.params.id));
    if (!video) {
        return res.status(404).json({ error: 'Відео не знайдено' });
    }
    // Збільшуємо лічильник переглядів
    video.views += 1;
    writeData(data);

    const author = data.users.find(u => u.id === video.authorId);
    const commentsWithUsers = video.comments.map(c => {
        const commentUser = data.users.find(u => u.id === c.userId);
        return { ...c, username: commentUser ? commentUser.username : 'Невідомий' };
    });

    res.json({
        ...video,
        comments: commentsWithUsers,
        authorName: author ? author.username : 'Невідомий',
        channelName: author ? author.channel : 'Невідомий канал',
        subscriberCount: author ? author.subscribers.length : 0,
    });
});

// Рівень 2: Додати відео (завантаження)
app.post('/api/videos', authenticate, (req, res) => {
    const { title, description, url, thumbnail } = req.body;
    if (!title || !url) {
        return res.status(400).json({ error: 'Назва та URL обовязкові' });
    }

    const data = readData();
    const newVideo = {
        id: data.nextVideoId++,
        title,
        description: description || '',
        url,
        thumbnail: thumbnail || 'https://placehold.co/320x180/95a5a6/white?text=Video',
        authorId: req.user.id,
        views: 0,
        createdAt: new Date().toISOString(),
        comments: [],
        sharedWith: [],
    };
    data.videos.push(newVideo);

    // Рівень 4: сповіщення підписникам каналу
    const author = data.users.find(u => u.id === req.user.id);
    if (author && author.subscribers.length > 0) {
        author.subscribers.forEach(subId => {
            data.notifications.push({
                id: data.nextNotificationId++,
                userId: subId,
                type: 'new_video',
                message: `Нове відео "${title}" на каналі "${author.channel}"`,
                videoId: newVideo.id,
                read: false,
                createdAt: new Date().toISOString(),
            });
        });
    }

    writeData(data);
    res.status(201).json(newVideo);
});

// Рівень 3: Видалити відео (тільки автор або адмін)
app.delete('/api/videos/:id', authenticate, (req, res) => {
    const data = readData();
    const videoIndex = data.videos.findIndex(v => v.id === parseInt(req.params.id));
    if (videoIndex === -1) {
        return res.status(404).json({ error: 'Відео не знайдено' });
    }
    const video = data.videos[videoIndex];
    if (video.authorId !== req.user.id && req.user.role !== 'admin') {
        return res.status(403).json({ error: 'Недостатньо прав для видалення' });
    }
    data.videos.splice(videoIndex, 1);
    writeData(data);
    res.json({ message: 'Відео видалено' });
});

// ============================================================
// Рівень 2: Коментарі
// ============================================================

app.post('/api/videos/:id/comments', authenticate, (req, res) => {
    const { text } = req.body;
    if (!text) {
        return res.status(400).json({ error: 'Текст коментаря обовязковий' });
    }

    const data = readData();
    const video = data.videos.find(v => v.id === parseInt(req.params.id));
    if (!video) {
        return res.status(404).json({ error: 'Відео не знайдено' });
    }

    const newComment = {
        id: data.nextCommentId++,
        userId: req.user.id,
        text,
        createdAt: new Date().toISOString(),
    };
    video.comments.push(newComment);
    writeData(data);

    res.status(201).json({
        ...newComment,
        username: req.user.username,
    });
});

// ============================================================
// Рівень 3: Користувачі та авторизація
// ============================================================

// Реєстрація
app.post('/api/users/register', (req, res) => {
    const { username, password, channel } = req.body;
    if (!username || !password) {
        return res.status(400).json({ error: 'Логін та пароль обовязкові' });
    }

    const data = readData();
    if (data.users.find(u => u.username === username)) {
        return res.status(400).json({ error: 'Користувач вже існує' });
    }

    const newUser = {
        id: data.nextUserId++,
        username,
        password,
        role: 'user',
        channel: channel || username + ' Channel',
        subscribers: [],
        createdAt: new Date().toISOString(),
    };
    data.users.push(newUser);
    writeData(data);

    res.status(201).json({ id: newUser.id, username: newUser.username, role: newUser.role, channel: newUser.channel });
});

// Логін
app.post('/api/users/login', (req, res) => {
    const { username, password } = req.body;
    const data = readData();
    const user = data.users.find(u => u.username === username && u.password === password);
    if (!user) {
        return res.status(401).json({ error: 'Невірний логін або пароль' });
    }
    res.json({ id: user.id, username: user.username, role: user.role, channel: user.channel });
});

// Список користувачів (для адміна)
app.get('/api/users', authenticate, (req, res) => {
    const data = readData();
    const users = data.users.map(u => ({
        id: u.id,
        username: u.username,
        role: u.role,
        channel: u.channel,
        subscriberCount: u.subscribers.length,
    }));
    res.json(users);
});

// Рівень 3: Зміна ролі (тільки адмін)
app.put('/api/users/:id/role', authenticate, requireAdmin, (req, res) => {
    const { role } = req.body;
    if (!['user', 'admin'].includes(role)) {
        return res.status(400).json({ error: 'Невірна роль' });
    }
    const data = readData();
    const user = data.users.find(u => u.id === parseInt(req.params.id));
    if (!user) {
        return res.status(404).json({ error: 'Користувача не знайдено' });
    }
    user.role = role;
    writeData(data);
    res.json({ message: 'Роль оновлено', username: user.username, role: user.role });
});

// ============================================================
// Рівень 3: Поділитися відео
// ============================================================

app.post('/api/videos/:id/share', authenticate, (req, res) => {
    const { targetUserId } = req.body;
    if (!targetUserId) {
        return res.status(400).json({ error: 'Вкажіть ID користувача' });
    }

    const data = readData();
    const video = data.videos.find(v => v.id === parseInt(req.params.id));
    if (!video) {
        return res.status(404).json({ error: 'Відео не знайдено' });
    }
    const targetUser = data.users.find(u => u.id === parseInt(targetUserId));
    if (!targetUser) {
        return res.status(404).json({ error: 'Користувача не знайдено' });
    }

    // Додаємо до списку поділених
    if (!video.sharedWith.includes(targetUser.id)) {
        video.sharedWith.push(targetUser.id);
    }

    // Сповіщення
    data.notifications.push({
        id: data.nextNotificationId++,
        userId: targetUser.id,
        type: 'shared_video',
        message: `${req.user.username} поділився з вами відео "${video.title}"`,
        videoId: video.id,
        read: false,
        createdAt: new Date().toISOString(),
    });

    writeData(data);
    res.json({ message: `Відео надіслано користувачу ${targetUser.username}` });
});

// Отримати відео, якими поділилися зі мною
app.get('/api/shared', authenticate, (req, res) => {
    const data = readData();
    const shared = data.videos
        .filter(v => v.sharedWith.includes(req.user.id))
        .map(v => {
            const author = data.users.find(u => u.id === v.authorId);
            return { ...v, authorName: author ? author.username : 'Невідомий' };
        });
    res.json(shared);
});

// ============================================================
// Рівень 4: Підписки на канали
// ============================================================

app.post('/api/channels/:userId/subscribe', authenticate, (req, res) => {
    const data = readData();
    const channelOwner = data.users.find(u => u.id === parseInt(req.params.userId));
    if (!channelOwner) {
        return res.status(404).json({ error: 'Канал не знайдено' });
    }
    if (channelOwner.id === req.user.id) {
        return res.status(400).json({ error: 'Не можна підписатися на себе' });
    }

    const alreadySubscribed = channelOwner.subscribers.includes(req.user.id);
    if (alreadySubscribed) {
        // Відписатися
        channelOwner.subscribers = channelOwner.subscribers.filter(id => id !== req.user.id);
        writeData(data);
        return res.json({ subscribed: false, subscriberCount: channelOwner.subscribers.length });
    } else {
        // Підписатися
        channelOwner.subscribers.push(req.user.id);
        writeData(data);
        return res.json({ subscribed: true, subscriberCount: channelOwner.subscribers.length });
    }
});

// Рівень 4: Сповіщення
app.get('/api/notifications', authenticate, (req, res) => {
    const data = readData();
    const userNotifications = data.notifications
        .filter(n => n.userId === req.user.id)
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    res.json(userNotifications);
});

// Позначити сповіщення як прочитане
app.put('/api/notifications/:id/read', authenticate, (req, res) => {
    const data = readData();
    const notification = data.notifications.find(
        n => n.id === parseInt(req.params.id) && n.userId === req.user.id
    );
    if (!notification) {
        return res.status(404).json({ error: 'Сповіщення не знайдено' });
    }
    notification.read = true;
    writeData(data);
    res.json({ message: 'Позначено як прочитане' });
});

// ============================================================
// Рівень 4: Список каналів
// ============================================================

app.get('/api/channels', (req, res) => {
    const data = readData();
    const channels = data.users.map(u => ({
        userId: u.id,
        channel: u.channel,
        username: u.username,
        subscriberCount: u.subscribers.length,
        videoCount: data.videos.filter(v => v.authorId === u.id).length,
    }));
    res.json(channels);
});

// ============================================================
// Головна сторінка — React SPA
// ============================================================

app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// ============================================================
// Запуск сервера
// ============================================================

app.listen(PORT, () => {
    console.log(`Сервер запущено: http://localhost:${PORT}`);
});
