// 管理者向け: 本の登録・編集と全体の貸出状況
(async () => {
    if (!App.requireLogin()) {
        return;
    }
    App.setupNav();
    if (!(await App.isAdmin())) {
        location.replace('/books.html');
        return;
    }

    const { el, emptyRow, showMessage, formatDateTime, loanStatus } = App;
    const bookBody = document.getElementById('admin-book-list');
    const loanBody = document.getElementById('admin-loan-list');
    const form = document.getElementById('book-form');
    const submitButton = document.getElementById('book-submit');
    const overdueOnly = document.getElementById('overdue-only');

    async function loadBooks() {
        try {
            const books = await App.request('/api/books');
            if (books.length === 0) {
                bookBody.replaceChildren(emptyRow(6, '登録された本はありません。'));
                return;
            }
            bookBody.replaceChildren(...books.map((book) => el('tr', {},
                el('td', {}, String(book.id)),
                el('td', {}, book.name),
                el('td', {}, book.author),
                el('td', {}, book.isbn || ''),
                el('td', { class: 'num' }, String(book.quantity)),
                el('td', { class: 'row-actions' },
                    el('button', { type: 'button', class: 'secondary small', onclick: () => edit(book) }, '編集'),
                ),
            )));
        } catch (e) {
            bookBody.replaceChildren(emptyRow(6, '本を取得できませんでした。'));
            showMessage(e.message, 'error');
        }
    }

    async function loadLoans() {
        try {
            const loans = await App.request('/api/loans?overdue=' + overdueOnly.checked);
            if (loans.length === 0) {
                loanBody.replaceChildren(emptyRow(6, '該当する貸出はありません。'));
                return;
            }
            loanBody.replaceChildren(...loans.map((loan) => el('tr', {},
                el('td', {}, loan.userName),
                el('td', {}, loan.bookName),
                el('td', {}, formatDateTime(loan.borrowedAt)),
                el('td', {}, loan.dueDate),
                el('td', {}, formatDateTime(loan.returnedAt)),
                el('td', {}, loanStatus(loan)),
            )));
        } catch (e) {
            loanBody.replaceChildren(emptyRow(6, '貸出を取得できませんでした。'));
            showMessage(e.message, 'error');
        }
    }

    function edit(book) {
        form.elements.id.value = book.id;
        form.elements.name.value = book.name;
        form.elements.author.value = book.author;
        form.elements.isbn.value = book.isbn || '';
        form.elements.quantity.value = book.quantity;
        submitButton.textContent = '更新する';
        form.scrollIntoView({ behavior: 'smooth' });
        form.elements.name.focus();
    }

    function resetForm() {
        form.reset();
        form.elements.id.value = '';
        form.elements.quantity.value = 1;
        submitButton.textContent = '登録する';
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const id = form.elements.id.value;
        const name = form.elements.name.value.trim();
        const author = form.elements.author.value.trim();
        const isbn = form.elements.isbn.value.trim();
        const quantity = Number(form.elements.quantity.value);

        if (!name || !author) {
            showMessage('書名と著者は必須です。', 'error');
            return;
        }
        if (!Number.isInteger(quantity) || quantity < 0) {
            showMessage('所蔵数は0以上の整数にしてください。', 'error');
            return;
        }

        submitButton.disabled = true;
        showMessage('');
        try {
            const body = { name, author, isbn: isbn || null, quantity };
            if (id) {
                await App.request('/api/books/' + id, { method: 'PUT', body });
                showMessage('本を更新しました。', 'success');
            } else {
                await App.request('/api/books', { method: 'POST', body });
                showMessage('本を登録しました。', 'success');
            }
            resetForm();
            loadBooks();
        } catch (e) {
            showMessage(e.message, 'error');
        } finally {
            submitButton.disabled = false;
        }
    });

    document.getElementById('book-reset').addEventListener('click', () => {
        resetForm();
        showMessage('');
    });

    overdueOnly.addEventListener('change', loadLoans);

    loadBooks();
    loadLoans();
})();
