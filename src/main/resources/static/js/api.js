// 全ページ共通の処理: JWT の保管、API 呼び出し、画面の共通部品
const App = (() => {
    const TOKEN_KEY = 'jwt';
    const ADMIN_KEY = 'isAdmin';

    class ApiError extends Error {
        constructor(status, message) {
            super(message);
            this.status = status;
        }
    }

    // JWT は sessionStorage に保存する（タブを閉じると消える）。Authorization ヘッダーで送る。
    function getToken() {
        try {
            return sessionStorage.getItem(TOKEN_KEY);
        } catch (e) {
            return null;
        }
    }

    function setToken(token) {
        sessionStorage.setItem(TOKEN_KEY, token);
        sessionStorage.removeItem(ADMIN_KEY); // 別の利用者でログインし直した場合に備える
    }

    function clearToken() {
        try {
            sessionStorage.removeItem(TOKEN_KEY);
            sessionStorage.removeItem(ADMIN_KEY);
        } catch (e) {
            // 保存領域が使えない場合は何もしない
        }
    }

    function defaultMessage(status) {
        switch (status) {
            case 400: return '入力内容を確認してください。';
            case 401: return '認証に失敗しました。';
            case 403: return 'この操作を行う権限がありません。';
            case 404: return '対象が見つかりません。';
            case 409: return '操作が競合しました。内容を確認してください。';
            default: return 'エラーが発生しました。';
        }
    }

    // JSON の API を呼ぶ。失敗時は ApiError を投げる。
    async function request(path, { method = 'GET', body, auth = true } = {}) {
        const headers = {};
        if (body !== undefined) {
            headers['Content-Type'] = 'application/json';
        }
        const token = getToken();
        if (auth && token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        let res;
        try {
            res = await fetch(path, {
                method,
                headers,
                body: body !== undefined ? JSON.stringify(body) : undefined,
            });
        } catch (e) {
            throw new ApiError(0, '通信に失敗しました。');
        }

        let data = null;
        const text = await res.text();
        if (text) {
            try {
                data = JSON.parse(text);
            } catch (e) {
                data = null;
            }
        }

        if (!res.ok) {
            if (res.status === 401 && auth) {
                // 未ログインまたは期限切れ
                clearToken();
                location.replace('/index.html');
            }
            const message = res.status === 404 ? defaultMessage(404) : (data && data.message) || defaultMessage(res.status);
            throw new ApiError(res.status, message);
        }
        return data;
    }

    function requireLogin() {
        if (!getToken()) {
            location.replace('/index.html');
            return false;
        }
        return true;
    }

    // 管理者専用の API が通るかで管理者かどうかを判断する
    async function isAdmin() {
        const cached = sessionStorage.getItem(ADMIN_KEY);
        if (cached !== null) {
            return cached === '1';
        }
        try {
            await request('/api/loans?overdue=true');
            sessionStorage.setItem(ADMIN_KEY, '1');
            return true;
        } catch (e) {
            if (e.status === 403) {
                sessionStorage.setItem(ADMIN_KEY, '0');
            }
            return false;
        }
    }

    // ログアウトボタンと管理メニューの表示を設定する
    function setupNav() {
        const logout = document.getElementById('logout-button');
        if (logout) {
            logout.addEventListener('click', () => {
                clearToken();
                location.href = '/index.html';
            });
        }
        const adminLink = document.getElementById('admin-link');
        if (adminLink) {
            isAdmin().then((admin) => {
                adminLink.hidden = !admin;
            });
        }
    }

    // #message にメッセージを表示する。type は 'error' | 'success'。text が空なら消す。
    function showMessage(text, type) {
        const box = document.getElementById('message');
        if (!box) {
            return;
        }
        box.textContent = text || '';
        box.className = 'message' + (type ? ' ' + type : '');
    }

    // DOM 要素を作る。文字列は textContent として扱うので HTML は解釈されない。
    function el(tag, attrs = {}, ...children) {
        const element = document.createElement(tag);
        for (const [key, value] of Object.entries(attrs)) {
            if (key === 'class') {
                element.className = value;
            } else if (key.startsWith('on')) {
                element.addEventListener(key.slice(2), value);
            } else if (value !== false && value != null) {
                element.setAttribute(key, value === true ? '' : value);
            }
        }
        for (const child of children.flat()) {
            element.append(child);
        }
        return element;
    }

    function emptyRow(colspan, text) {
        return el('tr', {}, el('td', { class: 'empty', colspan }, text));
    }

    function badge(text, kind) {
        return el('span', { class: 'badge ' + kind }, text);
    }

    // 日時を Asia/Tokyo の YYYY-MM-DD にする
    function formatDateTime(value) {
        if (!value) {
            return '';
        }
        return new Date(value).toLocaleDateString('sv-SE', { timeZone: 'Asia/Tokyo' });
    }

    function todayJst() {
        return new Date().toLocaleDateString('sv-SE', { timeZone: 'Asia/Tokyo' });
    }

    // YYYY-MM-DD に日数を足す
    function addDays(dateText, days) {
        const date = new Date(dateText + 'T00:00:00Z');
        date.setUTCDate(date.getUTCDate() + days);
        return date.toISOString().slice(0, 10);
    }

    // 未返却の貸出の状態。期限当日は期限内、翌日から延滞。
    function loanStatus(loan) {
        if (loan.returnedAt) {
            return badge('返却済み', 'ok');
        }
        const today = todayJst();
        if (loan.dueDate < today) {
            return badge('延滞中', 'ng');
        }
        if (loan.dueDate === today) {
            return badge('本日まで', 'warn');
        }
        return badge('貸出中', 'ok');
    }

    return {
        ApiError, request, getToken, setToken, clearToken, requireLogin, isAdmin, setupNav,
        showMessage, el, emptyRow, badge, formatDateTime, todayJst, addDays, loanStatus,
    };
})();
