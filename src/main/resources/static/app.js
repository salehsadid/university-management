// Simple frontend for the University API (plain JavaScript, no libraries).
// Uses the same session login as Postman: GET /api/auth/csrf -> POST /api/auth/login.

const RESOURCES = {
    students: {
        title: 'Students',
        single: 'Student',
        path: '/api/students',
        fields: [
            { name: 'studentId', label: 'Student ID', required: true },
            { name: 'name', label: 'Name', required: true },
            { name: 'email', label: 'Email', type: 'email', required: true },
            { name: 'password', label: 'Password', type: 'password' },
            { name: 'department', label: 'Department', required: true },
            { name: 'semester', label: 'Semester', required: true },
            { name: 'phone', label: 'Phone' }
        ]
    },
    teachers: {
        title: 'Teachers',
        single: 'Teacher',
        path: '/api/teachers',
        fields: [
            { name: 'name', label: 'Name', required: true },
            { name: 'email', label: 'Email', type: 'email', required: true },
            { name: 'password', label: 'Password', type: 'password' },
            { name: 'department', label: 'Department', required: true },
            { name: 'designation', label: 'Designation', required: true },
            { name: 'phone', label: 'Phone' }
        ]
    },
    admins: {
        title: 'Admins',
        single: 'Admin',
        path: '/api/admins',
        fields: [
            { name: 'name', label: 'Name', required: true },
            { name: 'email', label: 'Email', type: 'email', required: true },
            { name: 'password', label: 'Password', type: 'password' }
        ]
    }
};

// Fields a student or teacher may change on their own profile (PUT /me)
const PROFILE_FIELDS = [
    { name: 'name', label: 'Name', required: true },
    { name: 'email', label: 'Email', type: 'email', required: true },
    { name: 'password', label: 'Password', type: 'password' },
    { name: 'phone', label: 'Phone' }
];

const $ = id => document.getElementById(id);

let currentUser = null;   // { email, role }
let currentTab = 'students';
let profile = null;       // logged-in student/teacher profile
let onFormSubmit = null;  // what "Save" in the dialog does

// ---- API helper ----

class ApiError extends Error {
    constructor(status, body) {
        super(errorText(status, body));
        this.status = status;
    }
}

function errorText(status, body) {
    if (!body) return `Request failed (HTTP ${status})`;
    let text = body.detail || body.error || `Request failed (HTTP ${status})`;
    if (body.errors) text += ': ' + Object.values(body.errors).join(', ');
    return text;
}

async function api(method, path, body) {
    const headers = {};
    if (method !== 'GET') {
        // Every POST/PUT/DELETE needs the CSRF token
        const csrf = await fetch('/api/auth/csrf').then(res => res.json());
        headers[csrf.headerName] = csrf.token;
    }
    if (body !== undefined) headers['Content-Type'] = 'application/json';

    const res = await fetch(path, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    const data = await res.json().catch(() => null);  // 204 and plain 401 have no body
    if (!res.ok) throw new ApiError(res.status, data);
    return data;
}

function handleError(err) {
    if (err.status === 401) {
        showLogin();
        toast('Your session has ended. Please log in again.');
    } else {
        toast(err.message, true);
    }
}

// ---- Views ----

function show(view) {
    for (const id of ['login-view', 'admin-view', 'profile-view']) {
        $(id).hidden = id !== view;
    }
    $('user-box').hidden = view === 'login-view';
}

function showLogin() {
    currentUser = null;
    currentTab = 'students';
    $('login-form').reset();
    setPasswordVisible($('login-form').querySelector('.password-wrap'), false);
    $('login-error').textContent = '';
    show('login-view');
}

function openDashboard() {
    $('user-info').textContent = `${currentUser.email} (${currentUser.role})`;
    if (currentUser.role === 'ADMIN') {
        show('admin-view');
        loadList();
    } else {
        show('profile-view');
        loadProfile();
    }
}

async function start() {
    try {
        currentUser = await api('GET', '/api/auth/me');
        openDashboard();
    } catch {
        showLogin();
    }
}

// ---- Login / logout ----

$('login-form').addEventListener('submit', async event => {
    event.preventDefault();
    const form = new FormData(event.target);
    try {
        currentUser = await api('POST', '/api/auth/login', {
            email: form.get('email'),
            password: form.get('password')
        });
        openDashboard();
    } catch (err) {
        $('login-error').textContent = err.message;
    }
});

$('logout-btn').addEventListener('click', async () => {
    try {
        await api('POST', '/api/auth/logout');
    } catch {
        // session may already be gone
    }
    showLogin();
});

// ---- Admin: list + CRUD ----

document.querySelectorAll('.tabs button').forEach(tab => {
    tab.addEventListener('click', () => {
        currentTab = tab.dataset.tab;
        loadList();
    });
});

async function loadList() {
    const resource = RESOURCES[currentTab];
    document.querySelectorAll('.tabs button')
        .forEach(tab => tab.classList.toggle('active', tab.dataset.tab === currentTab));
    $('list-title').textContent = resource.title;
    try {
        renderTable(resource, await api('GET', resource.path));
    } catch (err) {
        handleError(err);
    }
}

function renderTable(resource, rows) {
    const columns = ['id', ...resource.fields.map(f => f.name).filter(name => name !== 'password')];
    const labels = { id: 'ID' };
    resource.fields.forEach(f => labels[f.name] = f.label);

    const table = $('list-table');
    table.replaceChildren();

    const headRow = table.createTHead().insertRow();
    for (const column of columns) headRow.appendChild(textElement('th', labels[column]));
    headRow.appendChild(textElement('th', 'Actions'));

    const body = table.createTBody();
    if (rows.length === 0) {
        const cell = body.insertRow().insertCell();
        cell.colSpan = columns.length + 1;
        cell.className = 'empty';
        cell.textContent = `No ${resource.title.toLowerCase()} yet. Click "+ Add" to create one.`;
        return;
    }

    for (const row of rows) {
        const tr = body.insertRow();
        for (const column of columns) tr.insertCell().textContent = row[column] ?? '-';

        const actions = tr.insertCell();
        actions.className = 'row-actions';
        actions.append(button('Edit', 'secondary', () => openEdit(resource, row)));
        // An admin cannot delete their own account from here
        if (!(currentTab === 'admins' && row.email === currentUser.email)) {
            actions.append(button('Delete', 'danger', () => deleteRow(resource, row)));
        }
    }
}

$('add-btn').addEventListener('click', () => {
    const resource = RESOURCES[currentTab];
    openForm(`Add ${resource.single}`, resource.fields, {}, true, async data => {
        await api('POST', resource.path, data);
        toast(`${resource.single} created`);
        loadList();
    });
});

function openEdit(resource, row) {
    openForm(`Edit ${resource.single} #${row.id}`, resource.fields, row, false, async data => {
        await api('PUT', `${resource.path}/${row.id}`, data);
        toast(`${resource.single} updated`);
        loadList();
    });
}

async function deleteRow(resource, row) {
    if (!confirm(`Delete ${resource.single.toLowerCase()} "${row.name}"?`)) return;
    try {
        await api('DELETE', `${resource.path}/${row.id}`);
        toast(`${resource.single} deleted`);
        loadList();
    } catch (err) {
        handleError(err);
    }
}

// ---- Student / Teacher: own profile ----

function profileResource() {
    return currentUser.role === 'STUDENT' ? RESOURCES.students : RESOURCES.teachers;
}

async function loadProfile() {
    const resource = profileResource();
    try {
        profile = await api('GET', `${resource.path}/me`);
    } catch (err) {
        handleError(err);
        return;
    }

    const details = $('profile-details');
    details.replaceChildren();
    for (const field of resource.fields) {
        if (field.type === 'password') continue;
        details.append(textElement('dt', field.label), textElement('dd', profile[field.name] ?? '-'));
    }
}

$('edit-profile-btn').addEventListener('click', () => {
    openForm('Edit My Profile', PROFILE_FIELDS, profile, false, async data => {
        await api('PUT', `${profileResource().path}/me`, data);
        if (data.email !== currentUser.email || data.password) {
            // The server ends the session when email or password changes
            showLogin();
            toast('Profile updated. Please log in again with your new details.');
        } else {
            toast('Profile updated');
            loadProfile();
        }
    });
});

$('delete-profile-btn').addEventListener('click', async () => {
    if (!confirm('Delete your account? This cannot be undone.')) return;
    try {
        await api('DELETE', `${profileResource().path}/me`);
        showLogin();
        toast('Your account has been deleted');
    } catch (err) {
        handleError(err);
    }
});

// ---- Add/Edit dialog ----

function openForm(title, fields, values, isCreate, submit) {
    $('form-title').textContent = title;
    $('form-error').textContent = '';
    $('form-fields').replaceChildren(...fields.map(field => {
        const input = document.createElement('input');
        input.name = field.name;
        input.type = field.type || 'text';
        input.value = field.type === 'password' ? '' : (values[field.name] ?? '');
        input.required = Boolean(field.required) || (isCreate && field.type === 'password');
        if (field.type === 'password' && !isCreate) input.placeholder = 'Leave empty to keep current password';

        const label = textElement('label', field.label);
        label.append(field.type === 'password' ? withPasswordToggle(input) : input);
        return label;
    }));
    onFormSubmit = submit;
    $('form-dialog').showModal();
}

$('entity-form').addEventListener('submit', async event => {
    event.preventDefault();
    const data = {};
    for (const [key, value] of new FormData(event.target)) {
        const cleaned = key === 'password' ? value : value.trim();
        data[key] = cleaned === '' ? null : cleaned;  // empty optional fields are sent as null
    }
    try {
        await onFormSubmit(data);
        $('form-dialog').close();
    } catch (err) {
        if (err.status === 401) {
            $('form-dialog').close();
            handleError(err);
        } else {
            $('form-error').textContent = err.message;
        }
    }
});

$('cancel-btn').addEventListener('click', () => $('form-dialog').close());

// ---- Small helpers ----

function textElement(tag, text) {
    const element = document.createElement(tag);
    element.textContent = text;
    return element;
}

function button(text, className, onClick) {
    const element = textElement('button', text);
    element.type = 'button';
    element.className = className;
    element.addEventListener('click', onClick);
    return element;
}

const EYE_ICON = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">'
    + '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>';
const EYE_OFF_ICON = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">'
    + '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>'
    + '<line x1="1" y1="1" x2="23" y2="23"/></svg>';

// Puts a password input in a wrapper with an eye button that shows/hides the password
function withPasswordToggle(input) {
    const wrap = document.createElement('div');
    wrap.className = 'password-wrap';
    if (input.parentNode) input.replaceWith(wrap);

    const toggle = button('', 'eye-btn', () => setPasswordVisible(wrap, input.type === 'password'));
    wrap.append(input, toggle);
    setPasswordVisible(wrap, false);
    return wrap;
}

function setPasswordVisible(wrap, visible) {
    wrap.querySelector('input').type = visible ? 'text' : 'password';
    const toggle = wrap.querySelector('.eye-btn');
    toggle.innerHTML = visible ? EYE_OFF_ICON : EYE_ICON;
    toggle.title = visible ? 'Hide password' : 'Show password';
    toggle.setAttribute('aria-label', toggle.title);
}

let toastTimer;

function toast(message, isError = false) {
    const box = $('toast');
    box.textContent = message;
    box.className = isError ? 'toast-error' : '';
    box.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => box.hidden = true, 3500);
}

withPasswordToggle($('login-form').elements.password);
start();
