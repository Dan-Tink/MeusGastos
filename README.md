# Meus Gastos

App Android (Kotlin + Jetpack Compose + Room) para registrar despesas como numa planilha.

## Como abrir e rodar

1. Instale o Android Studio (versão recente).
2. `File > Open` e escolha esta pasta (`MeusGastos`). Aguarde o Gradle sincronizar.
3. Para testar: conecte o celular com depuração USB ou crie um emulador e clique em Run.
4. Para gerar o APK: `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

## O que o app faz

- Painel do mês no topo: ganhos, gastos fixos, gastos variáveis e saldo, atualizados sozinhos.
- Ganhos (salário, extras) lançados por mês; toque em "Ganhos" para adicionar, editar ou excluir.
- Cada tipo de despesa é marcado como fixo ou variável (menu de tipos, toque no rótulo).
- Lançamento em 3 passos: tipo, descrição e valor (data de hoje por padrão, editável).
- Lista do mês com navegação entre meses.
- Busca por palavra-chave (como Ctrl+F), em todos os meses.
- Filtros por tipo, faixa de valor e período, com ordenação por data ou valor.
- Resumo com total e valor gasto por tipo.
- Editar e excluir gastos; adicionar, renomear e excluir tipos de despesa.
- Dados salvos no aparelho (Room/SQLite), sem internet.
