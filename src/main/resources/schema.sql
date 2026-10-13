create table if not exists users (
    id serial primary key,
    mail varchar(255) unique not null, -- メール
    name varchar(32) not null, -- ユーザ名
    password varchar(60) not null, -- PW(BCrypt採用)
    is_admin boolean not null default false -- 管理者フラグ
);

create table if not exists books (
    id serial primary key,
    name varchar(255) not null, -- 本の名前
    author varchar(255) not null, -- 著者
    isbn varchar(13), -- ISBN
    quantity integer not null, -- 本の冊数
    is_available boolean default true not null, -- 本削除フラグ
--     transcription varchar(255) not null,
--     publisher varchar(255) not null, -- これ別でテーブル持ってそこからアクセスする方がいいかもしれない
    check (quantity >= 0) -- 冊数0未満を弾く
);

create table if not exists loans ( -- 借りた記録
    id serial primary key,
    borrowed_user integer not null, -- 借りたユーザ
    borrowed_book integer not null, -- 借りた本
    borrowed_at timestamptz default current_timestamp not null, -- 借りた日時
    due_date date not null, -- 返却期限日
    returned_at timestamptz default null, -- 返した日時
    foreign key (borrowed_user) references users(id),
    foreign key (borrowed_book) references books(id)
);
