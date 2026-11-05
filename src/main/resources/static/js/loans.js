// 自分の貸出の表示・返却・延長
(() => {
    if (!App.requireLogin()) {
        return;
    }
    App.setupNav();

    const { el, emptyRow, showMessage, formatDateTime, loanStatus } = App;
    const currentBody = document.getElementById('current-loans');
    const returnedBody = document.getElementById('returned-loans');
    const dialog = document.getElementById('extend-dialog');
    const form = document.getElementById('extend-form');
    const dueInput = document.getElementById('extend-due-date');
    let target = null;

    async function load() {
        try {
            const loans = await App.request('/api/loans/me');
            render(loans);
        } catch (e) {
            currentBody.replaceChildren(emptyRow(6, '貸出を取得できませんでした。'));
            returnedBody.replaceChildren(emptyRow(5, ''));
            showMessage(e.message, 'error');
        }
    }

    function render(loans) {
        const current = loans.filter((loan) => !loan.returnedAt).sort((a, b) => a.dueDate.localeCompare(b.dueDate));
        const returned = loans.filter((loan) => loan.returnedAt).sort((a, b) => b.returnedAt.localeCompare(a.returnedAt));

        if (current.length === 0) {
            currentBody.replaceChildren(emptyRow(6, '借りている本はありません。'));
        } else {
            currentBody.replaceChildren(...current.map((loan) => el('tr', {},
                el('td', {}, loan.bookName),
                el('td', {}, loan.bookAuthor),
                el('td', {}, formatDateTime(loan.borrowedAt)),
                el('td', {}, loan.dueDate),
                el('td', {}, loanStatus(loan)),
                el('td', { class: 'row-actions' },
                    el('button', { type: 'button', class: 'small', onclick: () => returnLoan(loan) }, '返却'),
                    el('button', { type: 'button', class: 'secondary small', onclick: () => openExtend(loan) }, '延長'),
                ),
            )));
        }

        if (returned.length === 0) {
            returnedBody.replaceChildren(emptyRow(5, '返却済みの貸出はありません。'));
        } else {
            returnedBody.replaceChildren(...returned.map((loan) => el('tr', {},
                el('td', {}, loan.bookName),
                el('td', {}, loan.bookAuthor),
                el('td', {}, formatDateTime(loan.borrowedAt)),
                el('td', {}, loan.dueDate),
                el('td', {}, formatDateTime(loan.returnedAt)),
            )));
        }
    }

    async function returnLoan(loan) {
        showMessage('');
        try {
            await App.request('/api/loans/' + loan.id, { method: 'DELETE' });
            showMessage('「' + loan.bookName + '」を返却しました。', 'success');
        } catch (e) {
            showMessage(e.message, 'error');
        }
        load();
    }

    function openExtend(loan) {
        target = loan;
        document.getElementById('extend-target').textContent =
            '「' + loan.bookName + '」 現在の返却期限: ' + loan.dueDate;
        dueInput.min = App.addDays(loan.dueDate, 1);
        dueInput.value = App.addDays(loan.dueDate, 7);
        dialog.showModal();
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!dueInput.value || dueInput.value <= target.dueDate) {
            showMessage('現在の期限より後の日付を選んでください。', 'error');
            dialog.close();
            return;
        }
        const dueDate = dueInput.value;
        dialog.close();
        try {
            await App.request('/api/loans/' + target.id + '/due-date', { method: 'PUT', body: { dueDate } });
            showMessage('返却期限を ' + dueDate + ' に延長しました。', 'success');
        } catch (e) {
            showMessage(e.message, 'error');
        }
        load();
    });

    document.getElementById('extend-cancel').addEventListener('click', () => dialog.close());

    load();
})();
