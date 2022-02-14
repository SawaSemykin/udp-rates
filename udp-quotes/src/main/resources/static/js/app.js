let client = Stomp.over(new SockJS('/elvls'));
client.debug = null;

const updateElvls = (elvl) => {
    const row  = document.getElementById(elvl.isin);
    updateCell(row.cells[1], elvl.value);
}

const updateCell = (cell, newValue) => {
    const currentValue = parseFloat(cell.innerText);
    if (newValue > currentValue) {
        cell.style.color = "green";
    } else if (newValue < currentValue) {
        cell.style.color = "red";
    }
    cell.innerHTML = newValue;
}

client.connect({}, (frame) => {
    client.subscribe("/topic/elvls", (frame) => updateElvls(JSON.parse(frame.body)));
});