/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.REST;

import OhmOMatic.ProtoBuffer.HomeOuterClass.parametriCasaReq;
import OhmOMatic.ProtoBuffer.HomeOuterClass.parametriCasaRes;
import OhmOMatic.ProtoBuffer.StatOuterClass.parametriStatisticaReq;
import OhmOMatic.ProtoBuffer.StatOuterClass.parametriStatisticaRes;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

@Path("OOM")
public final class OOM
{

    //region Casa
    @PUT
    @Path("/iscriviCasa")
    public parametriCasaRes iscriviCasa(parametriCasaReq par)
    {
        final var res = parametriCasaRes
                .newBuilder()
                .setOk(false)
                .setErrore("Non posso aggiungere la tua casa perché mi fai schifo!")
                .build();

        return res;
    }

    @PUT
    @Path("/disiscriviCasa")
    public parametriCasaRes disiscriviCasa(parametriCasaReq par)
    {
        final var res = parametriCasaRes
                .newBuilder()
                .setOk(false)
                .setErrore("Non posso aggiungere la tua casa perché mi fai schifo!")
                .build();

        return res;
    }
    //endregion


    //region Statistiche
    @PUT
    @Path("/aggiungiStatisticaLocale")
    public parametriStatisticaRes aggiungiStatisticaLocale(parametriStatisticaReq par)
    {
        final var res = parametriStatisticaRes
                .newBuilder()
                .setOk(false)
                .setErrore("Non posso aggiungere la tua statistica perché mi fai schifo!")
                .build();

        return res;
    }

    @PUT
    @Path("/aggiungiStatisticaGlobale")
    public parametriStatisticaRes aggiungiStatisticaGlobale(parametriStatisticaReq par)
    {
        final var res = parametriStatisticaRes
                .newBuilder()
                .setOk(false)
                .setErrore("Non posso aggiungere la tua statistica perché mi fai schifo!")
                .build();

        return res;
    }
    //endregion


}