async function runBackup(type) {
    const btn = event.target;
    const loader = document.getElementById('loading');
    const msg = document.getElementById('message');
    
    btn.disabled = true;
    loader.style.display = 'block';
    msg.className = 'alert';
    msg.style.display = 'none';

    try {
        const resp = await fetch(`/api/backup/${type}`);
        const text = await resp.text();
        msg.innerText = text;
        msg.classList.add(resp.ok ? 'alert-success' : 'alert-error');
    } catch (e) {
        msg.innerText = "Fallo de conexión con el servidor";
        msg.classList.add('alert-error');
    } finally {
        btn.disabled = false;
        loader.style.display = 'none';
    }
}