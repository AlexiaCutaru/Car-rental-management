/* Filtre locale pentru înregistrările deja încărcate. Nu schimbă disponibilitatea. */
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-filter-root]').forEach(root => {
        const input = root.querySelector('[data-search]');
        const chips = [...root.querySelectorAll('[data-filter]')];
        const items = [...root.querySelectorAll('[data-item]')];
        const empty = root.querySelector('[data-no-results]');
        const counter = root.querySelector('[data-result-count]');
        let selected = 'all';
        const apply = () => {
            const query = (input?.value || '').normalize('NFD')
                .replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('ro').trim();
            let count = 0;
            items.forEach(item => {
                const searchable = (item.dataset.searchText || item.textContent)
                    .normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('ro');
                const visible = (selected === 'all' || item.dataset.state === selected)
                    && searchable.includes(query);
                item.hidden = !visible;
                if (visible) count++;
            });
            if (empty) empty.hidden = count > 0 || items.length === 0;
            if (counter) counter.textContent = `${count} / ${items.length}`;
        };
        input?.addEventListener('input', apply);
        chips.forEach(chip => chip.addEventListener('click', () => {
            selected = chip.dataset.filter;
            chips.forEach(other => {
                const active = other === chip;
                other.classList.toggle('is-active', active);
                other.setAttribute('aria-pressed', String(active));
            });
            apply();
        }));
        apply();
    });
});
