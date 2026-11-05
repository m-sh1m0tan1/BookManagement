// 本の一覧・検索・詳細・貸出
(() => {
    if (!App.requireLogin()) {
        return;
    }
    App.setupNav();

    const { el, emptyRow, badge, showMessage } = App;
    const list = document.getElementById('book-list');
    const keywordInput = document.getElementById('keyword');
    const availableOnly = document.getElementById('available-only');
    const dialog = document.getElementById('book-detail');
    const borrowButton = document.getElementById('detail-borrow');
    let current = null;

    function isAvailable(book) {
        return book.available ?? book.isAvailable;
    }

    function statusBadge(book) {
        return isAvailable(book) ? badge('貸出可能', 'ok') : badge('貸出中', 'ng');
    }

    async function load() {
        const keyword = keywordInput.value.trim();
        let path = '/api/books';
        if (availableOnly.checked) {
            path = '/api/books/available?keyword=' + encodeURIComponent(keyword);
        } else if (keyword) {
            path = '/api/books/search/' + encodeURIComponent(keyword);
        }
        try {
            const books = await App.request(path);
            render(books);
        } catch (e) {
            list.replaceChildren(emptyRow(6, '本を取得できませんでした。'));
            showMessage(e.message, 'error');
        }
    }

    function render(books) {
        if (books.length === 0) {
            list.replaceChildren(emptyRow(6, '該当する本はありません。'));
            return;
        }
        list.replaceChildren(...books.map((book) => el('tr', {},
            el('td', {}, book.name),
            el('td', {}, book.author),
            el('td', {}, book.isbn || ''),
            el('td', { class: 'num' }, String(book.quantity)),
            el('td', {}, statusBadge(book)),
            el('td', { class: 'row-actions' },
                el('button', { type: 'button', class: 'secondary small', onclick: () => openDetail(book) }, '詳細'),
            ),
        )));
    }

    function openDetail(book) {
        current = book;
        document.getElementById('detail-name').textContent = book.name;
        document.getElementById('detail-author').textContent = book.author;
        document.getElementById('detail-isbn').textContent = book.isbn || '-';
        document.getElementById('detail-quantity').textContent = String(book.quantity);
        document.getElementById('detail-status').replaceChildren(statusBadge(book));
        borrowButton.disabled = !isAvailable(book);
        dialog.showModal();
    }

    document.getElementById('search-form').addEventListener('submit', (event) => {
        event.preventDefault();
        showMessage('');
        load();
    });

    document.getElementById('detail-close').addEventListener('click', () => dialog.close());

    borrowButton.addEventListener('click', async () => {
        borrowButton.disabled = true;
        try {
            const loan = await App.request('/api/loans', { method: 'POST', body: { bookId: current.id } });
            dialog.close();
            showMessage('「' + current.name + '」を借りました。返却期限は ' + loan.dueDate + ' です。', 'success');
            load();
        } catch (e) {
            dialog.close();
            showMessage(e.message, 'error');
            load();
        }
    });

    load();
})();
