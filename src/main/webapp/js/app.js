const API = "rest";
const $ = (id) => document.getElementById(id);

let token = null; // gardé en mémoire : il expire de toute façon après 15 minutes
let editingUE = null;
let editingModule = null;

/* ===================== API ===================== */

async function api(path, method = "GET", body) {
    const headers = {};
    if (body !== undefined) headers["Content-Type"] = "application/json";
    if (token) headers.Authorization = "Bearer " + token;

    const res = await fetch(`${API}/${path}`, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body)
    });

    if (res.status === 401 && token) setToken(null);
    if (!res.ok) throw new Error(await errorMessage(res));
    return res.status === 204 ? null : res.json();
}

async function errorMessage(res) {
    try {
        return (await res.json()).error || `Erreur ${res.status}`;
    } catch {
        return `Erreur ${res.status}`;
    }
}

async function run(action, success) {
    try {
        await action();
        if (success) showMessage(success);
    } catch (e) {
        showMessage(e.message, true);
    }
}

function showMessage(text, isError = false) {
    const message = $("message");
    message.textContent = text;
    message.className = isError ? "error" : "success";
    message.hidden = false;
}

/* ===================== Session ===================== */

function setToken(value) {
    token = value;
    const connected = token !== null;
    $("loginForm").hidden = connected;
    $("logged").hidden = !connected;
    document.querySelectorAll(".auth-only").forEach((el) => { el.hidden = !connected; });
    if (!connected) {
        resetUEForm();
        resetModuleForm();
    }
    run(refresh);
}

async function login(event) {
    event.preventDefault();
    const res = await fetch(`${API}/authentification`, {
        method: "POST",
        body: new URLSearchParams({ username: $("username").value, password: $("password").value })
    });
    if (!res.ok) {
        showMessage(await errorMessage(res), true);
        return;
    }
    $("loginForm").reset();
    $("message").hidden = true;
    setToken(await res.text());
}

/* ===================== Affichage ===================== */

function row(values, onEdit, onDelete) {
    const tr = document.createElement("tr");
    values.forEach((value) => {
        const td = document.createElement("td");
        td.textContent = value;
        tr.append(td);
    });
    const actions = document.createElement("td");
    actions.className = "row-actions";
    if (token) actions.append(button("Modifier", "secondary", onEdit), button("Supprimer", "danger", onDelete));
    tr.append(actions);
    return tr;
}

function button(label, className, onClick) {
    const b = document.createElement("button");
    b.type = "button";
    b.textContent = label;
    b.className = className;
    b.addEventListener("click", onClick);
    return b;
}

function fill(tbody, rows, colspan, emptyText) {
    if (rows.length > 0) {
        tbody.replaceChildren(...rows);
        return;
    }
    const td = document.createElement("td");
    td.colSpan = colspan;
    td.className = "empty";
    td.textContent = emptyText;
    const tr = document.createElement("tr");
    tr.append(td);
    tbody.replaceChildren(tr);
}

async function loadUE() {
    const semestre = $("filterSemestre").value;
    const list = await api("UE" + (semestre ? `?semestre=${semestre}` : ""));
    fill($("ueRows"), list.map((ue) => row(
        [ue.code, ue.domaine, ue.responsable, ue.credits, ue.semestre],
        () => editUE(ue),
        () => removeUE(ue.code)
    )), 6, "Aucune UE");
}

async function loadModules() {
    const codeUE = $("filterUE").value;
    const list = await api("modules" + (codeUE ? `?codeUE=${codeUE}` : ""));
    fill($("moduleRows"), list.map((m) => row(
        [m.matricule, m.nom, m.coefficient, m.volumeHoraire, m.type, m.codeUE],
        () => editModule(m),
        () => removeModule(m.matricule)
    )), 7, "Aucun module");
}

async function loadUEOptions() {
    const list = await api("UE");
    const options = list.map((ue) => new Option(`${ue.code} - ${ue.domaine}`, ue.code));
    const filter = $("filterUE");
    const selected = filter.value;
    filter.replaceChildren(new Option("Toutes les UE", ""),
        ...list.map((ue) => new Option(`${ue.code} - ${ue.domaine}`, ue.code)));
    filter.value = list.some((ue) => String(ue.code) === selected) ? selected : "";

    const moduleUE = $("moduleUE");
    const current = moduleUE.value;
    moduleUE.replaceChildren(...options);
    if (current) moduleUE.value = current;
}

async function refresh() {
    await Promise.all([loadUE(), loadUEOptions()]);
    await loadModules();
}

/* ===================== UE ===================== */

function editUE(ue) {
    editingUE = ue.code;
    $("ueFormTitle").textContent = `Modifier l'UE ${ue.code}`;
    $("ueCode").value = ue.code;
    $("ueCode").disabled = true;
    $("ueDomaine").value = ue.domaine;
    $("ueResponsable").value = ue.responsable;
    $("ueCredits").value = ue.credits;
    $("ueSemestre").value = ue.semestre;
    $("ueDomaine").focus();
}

function resetUEForm() {
    editingUE = null;
    $("ueForm").reset();
    $("ueCode").disabled = false;
    $("ueFormTitle").textContent = "Ajouter une UE";
}

function saveUE(event) {
    event.preventDefault();
    const ue = {
        code: Number($("ueCode").value),
        domaine: $("ueDomaine").value,
        responsable: $("ueResponsable").value,
        credits: Number($("ueCredits").value),
        semestre: Number($("ueSemestre").value)
    };
    const editing = editingUE !== null;
    run(async () => {
        await (editing ? api(`UE/${editingUE}`, "PUT", ue) : api("UE", "POST", ue));
        resetUEForm();
        await refresh();
    }, editing ? "UE modifiée" : "UE ajoutée");
}

function removeUE(code) {
    if (!confirm(`Supprimer l'UE ${code} ?`)) return;
    run(async () => {
        await api(`UE/${code}`, "DELETE");
        await refresh();
    }, "UE supprimée");
}

/* ===================== Modules ===================== */

function editModule(m) {
    editingModule = m.matricule;
    $("moduleFormTitle").textContent = `Modifier le module ${m.matricule}`;
    $("moduleMatricule").value = m.matricule;
    $("moduleMatricule").disabled = true;
    $("moduleNom").value = m.nom;
    $("moduleCoefficient").value = m.coefficient;
    $("moduleVolume").value = m.volumeHoraire;
    $("moduleType").value = m.type;
    $("moduleUE").value = m.codeUE;
    $("moduleNom").focus();
}

function resetModuleForm() {
    editingModule = null;
    $("moduleForm").reset();
    $("moduleMatricule").disabled = false;
    $("moduleFormTitle").textContent = "Ajouter un module";
}

function saveModule(event) {
    event.preventDefault();
    const module = {
        matricule: $("moduleMatricule").value,
        nom: $("moduleNom").value,
        coefficient: Number($("moduleCoefficient").value),
        volumeHoraire: Number($("moduleVolume").value),
        type: $("moduleType").value,
        codeUE: Number($("moduleUE").value)
    };
    const editing = editingModule !== null;
    run(async () => {
        const path = `modules/${encodeURIComponent(editingModule)}`;
        await (editing ? api(path, "PUT", module) : api("modules", "POST", module));
        resetModuleForm();
        await refresh();
    }, editing ? "Module modifié" : "Module ajouté");
}

function removeModule(matricule) {
    if (!confirm(`Supprimer le module ${matricule} ?`)) return;
    run(async () => {
        await api(`modules/${encodeURIComponent(matricule)}`, "DELETE");
        await refresh();
    }, "Module supprimé");
}

/* ===================== Démarrage ===================== */

function showPage(page) {
    const isUE = page === "UE";
    $("pageUE").hidden = !isUE;
    $("pageModules").hidden = isUE;
    $("tabUE").classList.toggle("active", isUE);
    $("tabModules").classList.toggle("active", !isUE);
    $("message").hidden = true;
}

$("tabUE").addEventListener("click", () => showPage("UE"));
$("tabModules").addEventListener("click", () => showPage("Modules"));
$("loginForm").addEventListener("submit", (event) => run(() => login(event)));
$("logout").addEventListener("click", () => setToken(null));
$("ueForm").addEventListener("submit", saveUE);
$("moduleForm").addEventListener("submit", saveModule);
$("ueCancel").addEventListener("click", resetUEForm);
$("moduleCancel").addEventListener("click", resetModuleForm);
$("filterSemestre").addEventListener("change", () => run(loadUE));
$("filterUE").addEventListener("change", () => run(loadModules));

run(refresh);
