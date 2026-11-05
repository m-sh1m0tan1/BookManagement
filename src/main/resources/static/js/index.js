// ログイン・新規登録
(() => {
    if (App.getToken()) {
        location.replace('/books.html');
        return;
    }

    async function submitAuth(path, payload, form) {
        const button = form.querySelector('button[type="submit"]');
        button.disabled = true;
        App.showMessage('');
        try {
            const res = await App.request(path, { method: 'POST', body: payload, auth: false });
            App.setToken(res.token);
            location.href = '/books.html';
        } catch (e) {
            const message = e.status === 401 ? 'メールアドレスまたはパスワードが違います。' : e.message;
            App.showMessage(message, 'error');
            button.disabled = false;
        }
    }

    document.getElementById('login-form').addEventListener('submit', (event) => {
        event.preventDefault();
        const form = event.currentTarget;
        const mail = form.elements.mail.value.trim();
        const plainPassword = form.elements.plainPassword.value;
        if (!mail || !plainPassword) {
            App.showMessage('メールアドレスとパスワードを入力してください。', 'error');
            return;
        }
        submitAuth('/api/auth/login', { mail, plainPassword }, form);
    });

    document.getElementById('register-form').addEventListener('submit', (event) => {
        event.preventDefault();
        const form = event.currentTarget;
        const mail = form.elements.mail.value.trim();
        const name = form.elements.name.value.trim();
        const plainPassword = form.elements.plainPassword.value;
        if (!mail || !name || !plainPassword) {
            App.showMessage('メールアドレス、表示名、パスワードをすべて入力してください。', 'error');
            return;
        }
        submitAuth('/api/auth/register', { mail, name, plainPassword }, form);
    });
})();
